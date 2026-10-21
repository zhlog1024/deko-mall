package com.deko.mall.common.rabbitmq.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.deko.mall.common.rabbitmq.config.RetryConfig;
import com.deko.mall.common.rabbitmq.constant.MessageHeaders;
import com.deko.mall.common.rabbitmq.constant.MessageStatus;
import com.deko.mall.common.rabbitmq.entity.MqMessageProcess;
import com.deko.mall.common.rabbitmq.service.MessageProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.util.Assert;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 消息监听器泛型基类
 *
 * @param <T> 消息体类型
 */
@Slf4j
public abstract class AbstractMessageListener<T> {

    private final MessageProcessService messageProcessService;
    private final RetryConfig retryConfig;
    private final ObjectMapper objectMapper;
    private final Class<T> messageType;

    /**
     * 构造函数
     *
     * @param messageProcessService 消息处理服务
     * @param retryConfig           重试配置
     * @param objectMapper          对象映射器，用于消息转换
     */
    @SuppressWarnings("unchecked")
    public AbstractMessageListener(MessageProcessService messageProcessService,
                                   RetryConfig retryConfig,
                                   ObjectMapper objectMapper) {
        this.messageProcessService = messageProcessService;
        this.retryConfig = retryConfig;
        this.objectMapper = objectMapper;

        // 通过反射获取泛型参数类型
        Type type = getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            this.messageType = (Class<T>) ((ParameterizedType) type).getActualTypeArguments()[0];
        } else {
            throw new IllegalArgumentException("消息监听器必须指定泛型参数类型");
        }
    }

    /**
     * 处理消息入口方法，可以被@RabbitListener注解的方法直接调用
     *
     * @param message 原始消息
     * @param channel 信道
     * @throws Exception 处理异常
     */
    public void handleMessage(Message message, Channel channel) throws Exception {
        Assert.notNull(message, "消息不能为空");
        Assert.notNull(channel, "Channel不能为空");
        Assert.notNull(message.getMessageProperties(), "消息属性不能为空");

        MessageProperties properties = message.getMessageProperties();
        long deliveryTag = properties.getDeliveryTag();
        String messageId = properties.getMessageId();
        String businessId = properties.getHeader(MessageHeaders.BUSINESS_ID);
        String messageType = properties.getHeader(MessageHeaders.MESSAGE_TYPE);
        String source = properties.getHeader(MessageHeaders.SOURCE);
        String consumerQueue = properties.getConsumerQueue();

        Assert.hasText(messageId, "消息ID不能为空");
        Assert.hasText(businessId, "业务ID不能为空");
        Assert.hasText(messageType, "消息类型不能为空");
        Assert.hasText(consumerQueue, "消费队列不能为空");

        try {
            // 1. 检查消息是否已处理成功
            if (messageProcessService.isProcessed(messageId, consumerQueue)) {
                log.info("消息已处理成功: messageId={}, type={}, businessId={}, source={}",
                        messageId, messageType, businessId, source);
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 2. 获取消息处理记录
            MqMessageProcess process = messageProcessService.findByMessageId(messageId);
            process.setConsumerQueue(consumerQueue);

            // 3. 检查重试次数
            if (!retryConfig.canRetry(process.getRetryCount())) {
                log.error("消息重试次数已达上限: messageId={}, type={}, businessId={}, source={}, retryCount={}",
                        messageId, messageType, businessId, source, process.getRetryCount());
                process.setStatus(MessageStatus.FAILED);
                messageProcessService.updateMessageProcess(process);
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 4. 更新状态为处理中
            process.setStatus(MessageStatus.PROCESSING);
            messageProcessService.updateMessageProcess(process);

            // 5. 获取并转换消息内容
            String messageBody = process.getMessageBody();
            if (messageBody == null) {
                throw new MessageConversionException("消息体为空");
            }

            // 6. 转换消息为目标类型
            T payload = convertMessageBody(messageBody);

            // 7. 处理消息 - 调用子类实现的业务处理方法
            processMessage(payload);

            // 8. 标记消息处理成功
            process.setStatus(MessageStatus.SUCCESS);
            messageProcessService.updateMessageProcess(process);
            channel.basicAck(deliveryTag, false);

            log.info("消息处理成功: messageId={}, type={}, businessId={}, source={}",
                    messageId, messageType, businessId, source);

        } catch (Exception e) {
            handleProcessError(e, messageId, businessId, messageType, source, consumerQueue, deliveryTag, channel);
            // 不向上抛出异常，已在 handleProcessError 中处理 nack/记录，避免外层容器重复处理导致连接/事务异常放大
        }
    }

    /**
     * 子类需要实现的消息处理方法
     *
     * @param payload 转换后的消息对象
     * @throws Exception 处理异常
     */
    protected abstract void processMessage(T payload) throws Exception;

    /**
     * 消息体转换方法
     */
    protected T convertMessageBody(String messageBody) throws JsonProcessingException {
        try {
            if (String.class.equals(messageType)) {
                @SuppressWarnings("unchecked")
                T result = (T) messageBody;
                return result;
            } else {
                return objectMapper.readValue(messageBody, messageType);
            }
        } catch (JsonProcessingException e) {
            log.error("消息转换失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 处理消息处理异常
     */
    private void handleProcessError(Exception e, String messageId, String businessId,
                                    String messageType, String source, String consumerQueue,
                                    long deliveryTag, Channel channel) {
        try {
            log.error("消息处理发生异常: messageId={}, type={}, businessId={}, source={}, error={}",
                    messageId, messageType, businessId, source, e.getMessage(), e);

            // 获取消息处理记录
            MqMessageProcess process = messageProcessService.findByMessageId(messageId);
            if (process != null) {
                // 增加重试次数
                process.setRetryCount(process.getRetryCount() + 1);
                process.setConsumerQueue(consumerQueue);

                // 更新状态为失败
                process.setStatus(MessageStatus.FAILED);
                process.setErrorMessage(e.getMessage());
                messageProcessService.updateMessageProcess(process);
            }

            // 消息重新入队，等待重试
            channel.basicNack(deliveryTag, false, true);
        } catch (Exception ex) {
            log.error("处理消息异常时出错: messageId={}, error={}", messageId, ex.getMessage(), ex);
        }
    }
}