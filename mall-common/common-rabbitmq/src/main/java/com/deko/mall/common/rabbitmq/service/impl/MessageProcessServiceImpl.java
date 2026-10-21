package com.deko.mall.common.rabbitmq.service.impl;

import com.deko.mall.common.rabbitmq.constant.MessageStatus;
import com.deko.mall.common.rabbitmq.entity.MqMessageProcess;
import com.deko.mall.common.rabbitmq.service.MessageProcessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息处理服务实现
 * 使用JdbcTemplate实现消息处理记录的持久化
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageProcessServiceImpl implements MessageProcessService {

    private static final String TABLE_NAME = "mq_message_process";
    private static final BeanPropertyRowMapper<MqMessageProcess> ROW_MAPPER =
            new BeanPropertyRowMapper<>(MqMessageProcess.class);
    // INSERT ... ON DUPLICATE KEY UPDATE：遇到同 messageId 自动转更新，不抛 DuplicateKeyException
    private static final String INSERT_SQL = "INSERT INTO " + TABLE_NAME +
            " (message_id, business_id, message_type, exchange, routing_key, " +
            "consumer_queue, message_body, status, retry_count, error_message, source) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE " +
            "status = VALUES(status), retry_count = VALUES(retry_count), error_message = VALUES(error_message), " +
            "message_body = VALUES(message_body), consumer_queue = VALUES(consumer_queue)";
    private static final String UPDATE_SQL = "UPDATE " + TABLE_NAME +
            " SET status = ?, retry_count = ?, error_message = ?, message_body = ? ,consumer_queue = ?  " +
            "WHERE message_id = ? ";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM " + TABLE_NAME + " WHERE message_id = ?";
    private static final String CHECK_PROCESSED_SQL = "SELECT COUNT(1) FROM " + TABLE_NAME +
            " WHERE message_id = ?  AND status = ?";
    private static final String DELETE_BY_BUSINESS_ID_SQL = "DELETE FROM " + TABLE_NAME + " WHERE business_id = ?";
    private final JdbcTemplate jdbcTemplate;

    @Override
    public boolean isProcessed(String messageId, String consumerQueue) {
        Assert.hasText(messageId, "消息ID不能为空");
        Assert.hasText(consumerQueue, "消费队列不能为空");

        try {
            int count = jdbcTemplate.queryForObject(CHECK_PROCESSED_SQL, Integer.class,
                    messageId, MessageStatus.SUCCESS);
            return count > 0;
        } catch (DataAccessException e) {
            log.error("查询消息处理状态异常: messageId={}, queue={}, error={}",
                    messageId, consumerQueue, e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMessageProcess(MqMessageProcess process) {
        try {
            validateMessageProcess(process);

            // 确保消费队列不为空
            if (process.getConsumerQueue() == null) {
                process.setConsumerQueue(process.getRoutingKey());
                log.warn("消费队列为空，使用routingKey作为默认值: messageId={}, routingKey={}",
                        process.getMessageId(), process.getRoutingKey());
            }

            // 设置默认值
            process.setRetryCount(process.getRetryCount() != null ? process.getRetryCount() : 0);
            process.setStatus(process.getStatus() != null ? process.getStatus() : MessageStatus.PENDING);

            jdbcTemplate.update(INSERT_SQL,
                    process.getMessageId(),
                    process.getBusinessId(),
                    process.getMessageType(),
                    process.getExchange(),
                    process.getRoutingKey(),
                    process.getConsumerQueue(),
                    process.getMessageBody(),
                    process.getStatus(),
                    process.getRetryCount(),
                    process.getErrorMessage(),
                    process.getSource()
            );

            log.info("保存消息处理记录成功: messageId={}, type={}, businessId={}, queue={}",
                    process.getMessageId(), process.getMessageType(),
                    process.getBusinessId(), process.getConsumerQueue());

        } catch (DataAccessException e) {
            log.error("保存消息处理记录异常: messageId={}, type={}, businessId={}, error={}",
                    process.getMessageId(), process.getMessageType(), process.getBusinessId(), e.getMessage());
            throw new RuntimeException("保存消息处理记录失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("保存消息处理记录发生未预期异常: messageId={}, error={}",
                    process != null ? process.getMessageId() : "unknown", e.getMessage());
            throw new RuntimeException("保存消息处理记录失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMessageProcess(MqMessageProcess process) {
        try {
            validateMessageProcess(process);
            // 确保消费队列不为空
            if (process.getConsumerQueue() == null) {
                process.setConsumerQueue(process.getRoutingKey());
                log.warn("更新时消费队列为空，使用routingKey作为默认值: messageId={}, routingKey={}",
                        process.getMessageId(), process.getRoutingKey());
            }

            process.setUpdateTime(LocalDateTime.now());

            int rows = jdbcTemplate.update(UPDATE_SQL,
                    process.getStatus(),
                    process.getRetryCount(),
                    process.getErrorMessage(),
                    process.getMessageBody(),
                    process.getConsumerQueue(),
                    process.getMessageId()
            );

            if (rows == 0) {
                log.warn("消息处理记录不存在，无法更新: messageId={}, queue={}",
                        process.getMessageId(), process.getConsumerQueue());
            } else {
                log.info("更新消息处理记录成功: messageId={}, type={}, businessId={}, status={}, queue={}",
                        process.getMessageId(), process.getMessageType(),
                        process.getBusinessId(), process.getStatus(), process.getConsumerQueue());
            }
        } catch (DataAccessException e) {
            log.error("更新消息处理记录异常: messageId={}, type={}, businessId={}, error={}",
                    process.getMessageId(), process.getMessageType(),
                    process.getBusinessId(), e.getMessage());
            throw new RuntimeException("更新消息处理记录失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("更新消息处理记录发生未预期异常: messageId={}, error={}",
                    process != null ? process.getMessageId() : "unknown", e.getMessage());
            throw new RuntimeException("更新消息处理记录失败: " + e.getMessage(), e);
        }
    }

    @Override
    public MqMessageProcess findByMessageId(String messageId) {
        Assert.hasText(messageId, "消息ID不能为空");

        try {
            List<MqMessageProcess> list = jdbcTemplate.query(SELECT_BY_ID_SQL, ROW_MAPPER, messageId);
            return list.isEmpty() ? null : list.get(0);
        } catch (DataAccessException e) {
            log.error("查询消息处理记录异常: messageId={}, error={}", messageId, e.getMessage());
            return null;
        }
    }

    private void validateMessageProcess(MqMessageProcess process) {
        Assert.notNull(process, "消息处理记录不能为空");
        Assert.hasText(process.getMessageId(), "消息ID不能为空");
        Assert.hasText(process.getBusinessId(), "业务ID不能为空");
        Assert.hasText(process.getMessageType(), "消息类型不能为空");
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void deleteByBusinessId(String businessId) {
        Assert.hasText(businessId, "业务ID不能为空");
        try {
            int rows = jdbcTemplate.update(DELETE_BY_BUSINESS_ID_SQL, businessId);
            log.info("删除消息处理记录: businessId={}, rows={}", businessId, rows);
        } catch (DataAccessException e) {
            log.error("删除消息处理记录异常: businessId={}, error={}", businessId, e.getMessage());
            throw new RuntimeException("删除消息处理记录失败: " + e.getMessage(), e);
        }
    }
}
