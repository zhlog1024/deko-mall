package com.deko.mall.common.rabbitmq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 消息发送参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSendDTO implements Serializable {

    /**
     * 交换机
     */
    private String exchange;

    /**
     * 路由键
     */
    private String routingKey;

    /**
     * 消息内容
     */
    private Object payload;

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
     * 延迟时间(毫秒)，仅延迟消息使用
     */
    private Long delayMillis;
}
