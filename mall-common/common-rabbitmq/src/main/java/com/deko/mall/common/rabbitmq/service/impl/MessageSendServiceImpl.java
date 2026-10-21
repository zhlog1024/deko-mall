package com.deko.mall.common.rabbitmq.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.deko.mall.common.rabbitmq.constant.MessageStatus;
import com.deko.mall.common.rabbitmq.dto.MessageSendDTO;
import com.deko.mall.common.rabbitmq.entity.MqMessageProcess;
import com.deko.mall.common.rabbitmq.service.MessageProcessService;
import com.deko.mall.common.rabbitmq.service.MessageSendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.UUID;

/**
 * 消息发送服务实现
 * 基于本地消息表模式，保证消息可靠发送
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageSendServiceImpl implements MessageSendService {

    private final RabbitTemplate rabbitTemplate;
    private final MessageProcessService messageProcessService;
    private final ObjectMapper objectMapper;
    private final MessageConverter messageConverter;

    @Override
    public void sendMessage(MessageSendDTO dto) {
        // 调用统一入口方法
        processSendMessage(dto);
    }

    @Override
    public void sendDelayMessage(MessageSendDTO dto) {
        if (dto.getDelayMillis() == null || dto.getDelayMillis() <= 0) {
            throw new RuntimeException("延迟时间必须大于0");
        }
        // 调用统一入口方法
        processSendMessage(dto);
    }

    /**
     * 消息发送统一入口方法
     *
     * 1. 保存消息记录（跟随业务事务，回滚则不留孤儿）
     * 2. 在事务中 → 注册 afterCommit 回调仅投递
     *    非事务   → 立即投递
     */
    private void processSendMessage(MessageSendDTO dto) {
        validateSendParams(dto);

        // 消息记录与业务同事务保存，业务回滚则消息记录也回滚
        final String messageId = saveMessageRecord(dto.getExchange(), dto.getRoutingKey(), dto.getPayload(),
                dto.getBusinessId(), dto.getMessageType(), dto.getSource(), dto.getDelayMillis());

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            sendAfterTransactionCommit(messageId);
        } else {
            sendMessageWithRetry(messageId);
        }
    }

    /**
     * 验证消息发送参数
     */
    private void validateSendParams(MessageSendDTO dto) {
        Assert.hasText(dto.getExchange(), "交换机不能为空");
        Assert.hasText(dto.getRoutingKey(), "路由键不能为空");
        Assert.notNull(dto.getPayload(), "消息内容不能为空");
        Assert.hasText(dto.getBusinessId(), "业务ID不能为空");
        Assert.hasText(dto.getMessageType(), "消息类型不能为空");
    }

    /**
     * 在当前事务提交后投递消息到 RabbitMQ（消息记录已在事务内保存）
     */
    private void sendAfterTransactionCommit(String messageId) {
        log.info("注册事务同步器: messageId={}", messageId);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    sendMessageWithRetry(messageId);
                } catch (Exception e) {
                    log.error("事务提交后消息投递失败: messageId={}, error={}", messageId, e.getMessage(), e);
                }
            }
        });
    }

    /**
     * 保存消息记录并发送消息
     * 先在独立事务中保存消息记录，然后确保记录已提交后发送消息
     */
    /**
     * @deprecated 请使用 {@link #sendMessage}，消息记录自动跟随调用方事务保存
     */
    @Deprecated
    public void saveAndSendMessage(String exchange, String routingKey, Object payload,
                                   String businessId, String messageType, String source, Long delayMillis) {
        try {
            // 先在独立事务中保存消息记录
            final String messageId = saveMessageRecordInNewTx(exchange, routingKey, payload, businessId, messageType, source, delayMillis);
            sendMessageWithRetry(messageId);
        } catch (Exception e) {
            log.error("保存消息记录失败: businessId={}, type={}, error={}",
                    businessId, messageType, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 带重试的消息发送
     * 确保消息记录可见后再发送
     */
    private void sendMessageWithRetry(String messageId) {
        try {
            // 尝试几次确保消息记录可见
            MqMessageProcess process = null;
            for (int i = 0; i < 3 && process == null; i++) {
                process = messageProcessService.findByMessageId(messageId);
                if (process == null && i < 2) {
                    try {
                        Thread.sleep(20); // 短暂等待确保数据可见
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            if (process != null) {
                sendRealMessage(messageId);
            } else {
                log.error("消息记录未找到，无法发送: messageId={}", messageId);
            }
        } catch (Exception e) {
            log.error("发送消息失败: messageId={}, error={}", messageId, e.getMessage(), e);
        }
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public String saveMessageRecordInNewTx(String exchange, String routingKey, Object payload,
                                           String businessId, String messageType, String source, Long delayMillis) {
        return saveMessageRecord(exchange, routingKey, payload, businessId, messageType, source, delayMillis);
    }

    /**
     * 记录消息状态更新失败的日志
     * 仅记录日志，不抛出异常，避免影响主流程
     */
    private void logUpdateStatusError(String messageId, String error) {
        log.error("消息状态更新失败: messageId={}, error={}", messageId, error);
    }

    /**
     * 创建并保存消息处理记录
     *
     * @return 消息ID
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public String saveMessageRecord(String exchange, String routingKey, Object payload,
                                    String businessId, String messageType, String source, Long delayMillis) {
        try {
            // 生成消息ID
            String messageId = UUID.randomUUID().toString().replace("-", "");

            // 创建消息处理记录
            MqMessageProcess process = new MqMessageProcess();
            process.setMessageId(messageId);
            process.setExchange(exchange);
            process.setRoutingKey(routingKey);
            process.setBusinessId(businessId);
            process.setMessageType(messageType);
            process.setStatus(MessageStatus.PENDING); // 初始状态为PENDING

            // 处理消息体 - 使用JSON序列化
            if (payload instanceof String) {
                // 字符串类型直接设置
                process.setMessageBody(String.valueOf(payload));
            } else {
                // 非字符串类型使用ObjectMapper序列化为JSON
                try {
                    process.setMessageBody(objectMapper.writeValueAsString(payload));
                } catch (JsonProcessingException e) {
                    log.error("消息体JSON序列化失败: {}", e.getMessage(), e);
                    process.setMessageBody("消息体序列化失败: " + e.getMessage());
                    process.setStatus(MessageStatus.FAILED);
                    process.setErrorMessage("消息体序列化失败: " + e.getMessage());
                }
            }

            // 设置来源服务
            process.setSource(source != null && !source.isEmpty() ? source : "unknown");

            // 设置消费队列信息
            process.setConsumerQueue(routingKey); // 先默认用routingKey作为队列名

            // 时间已经在实体类初始化时自动设置
            // LocalDateTime类型通过实体类自动生成

            // 保存消息处理记录到数据库
            messageProcessService.saveMessageProcess(process);
            log.debug("消息处理记录已保存: messageId={}", messageId);

            return messageId;
        } catch (Exception e) {
            log.error("保存消息处理记录失败: businessId={}, messageType={}, error={}",
                    businessId, messageType, e.getMessage());
            throw new RuntimeException("保存消息处理记录失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从数据库获取消息记录并实际发送消息
     * 使用独立事务更新消息状态，避免与业务事务相互影响
     *
     * @param messageId 消息ID
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void sendRealMessage(String messageId) {
        MqMessageProcess process = null;

        try {
            // 查询消息记录
            process = messageProcessService.findByMessageId(messageId);
            if (process == null) {
                log.error("无法发送消息，消息记录不存在: messageId={}", messageId);
                return;
            }

            // 获取消息基本信息
            String exchange = process.getExchange();
            String routingKey = process.getRoutingKey();
            String messageBody = process.getMessageBody();

            // 检查消息体是否为空
            if (messageBody == null) {
                log.error("消息体为空，无法发送: messageId={}", messageId);
                updateMessageStatus(process, MessageStatus.FAILED, "消息体为空，无法发送");
                return;
            }

            // 创建消息属性
            MessageProperties properties = new MessageProperties();
            properties.setMessageId(messageId);

            // 设置业务相关属性
            properties.setHeader("businessId", process.getBusinessId());
            properties.setHeader("messageType", process.getMessageType());
            properties.setHeader("source", process.getSource());
            properties.setTimestamp(new Date());
            properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT); // 消息持久化

            // 处理延迟消息
            long delay = extractDelayTime(process);
            if (delay > 0) {
                properties.setHeader("x-delay", delay);
                log.debug("设置延迟消息: messageId={}, delay={}", messageId, delay);
            }

            try {
                // 创建消息对象 - 使用消息转换器处理序列化
                // 如果messageBody是JSON字符串，尝试通过转换器处理
                Message message;
                if (messageBody.startsWith("{") || messageBody.startsWith("[")) {
                    // JSON字符串，交给messageConverter处理
                    message = messageConverter.toMessage(messageBody, properties);
                } else {
                    // 非JSON字符串，使用原始字节数组
                    message = new Message(messageBody.getBytes("UTF-8"), properties);
                }

                // 发送消息
                rabbitTemplate.send(exchange, routingKey, message);

                // 更新消息状态为成功
                updateMessageStatus(process, MessageStatus.PENDING, null);
                log.info("消息发送成功: messageId={}, exchange={}, routingKey={}", messageId, exchange, routingKey);
            } catch (java.io.UnsupportedEncodingException e) {
                // 编码异常
                log.error("消息体编码异常: messageId={}", messageId, e);
                updateMessageStatus(process, MessageStatus.FAILED, "消息体编码异常: " + e.getMessage());
            } catch (Exception e) {
                // 发送异常
                log.error("消息发送失败: messageId={}, businessId={}, type={}, error={}",
                        messageId, process.getBusinessId(), process.getMessageType(), e.getMessage());
                updateMessageStatus(process, MessageStatus.FAILED, "消息发送失败: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("发送消息过程发生异常: messageId={}, error={}", messageId, e.getMessage(), e);
            // 不抛出异常，保证事务高可用
        }
    }

    /**
     * 从消息处理记录中提取延迟时间
     * 针对DELAY类型的消息进行处理
     *
     * @param process 消息处理记录
     * @return 延迟时间(毫秒)，如果不是延迟消息返回0
     */
    private long extractDelayTime(MqMessageProcess process) {
        long delay = 0;

        // 判断是否是延迟消息
        if (process.getMessageType() != null && process.getMessageType().contains("DELAY")) {
            // 如果是延迟消息，尝试从消息体中提取延迟时间
            try {
                // 这里假定业务ID中包含了延迟时间，格式为"XXX_5000"（单位毫秒）
                String businessId = process.getBusinessId();
                if (businessId != null && businessId.contains("_")) {
                    String[] parts = businessId.split("_");
                    if (parts.length > 1) {
                        delay = Long.parseLong(parts[parts.length - 1]);
                    }
                } else {
                    // 默认延迟时间如果没有指定
                    delay = 5000L;  // 5秒
                }
            } catch (Exception e) {
                log.warn("提取延迟时间异常，使用默认值: {}", e.getMessage());
                delay = 5000L; // 默认延5秒
            }
        }

        return delay;
    }

    /**
     * 更新消息状态
     * 封装了消息状态更新的通用逻辑
     *
     * @param process      消息处理记录
     * @param status       新状态
     * @param errorMessage 错误信息，可以为空
     */
    private void updateMessageStatus(MqMessageProcess process, String status, String errorMessage) {
        try {
            process.setStatus(status);
            if (errorMessage != null) {
                process.setErrorMessage(errorMessage);
            }
            messageProcessService.updateMessageProcess(process);
        } catch (Exception e) {
            logUpdateStatusError(process.getMessageId(), e.getMessage());
        }
    }

    // 已经在类的前面定义了logUpdateStatusError方法
}
