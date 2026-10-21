package com.deko.mall.common.rabbitmq.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 消息处理记录
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class MqMessageProcess {

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 业务ID
     */
    private String businessId;

    /**
     * 消息类型
     */
    private String messageType;

    /**
     * 消息来源
     */
    private String source;

    /**
     * 交换机
     */
    private String exchange;

    /**
     * 路由键
     */
    private String routingKey;

    /**
     * 消费队列
     */
    private String consumerQueue;

    /**
     * 消息状态
     */
    private String status;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 错误消息
     */
    private String errorMessage;

    /**
     * 消息内容
     */
    private String messageBody;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
