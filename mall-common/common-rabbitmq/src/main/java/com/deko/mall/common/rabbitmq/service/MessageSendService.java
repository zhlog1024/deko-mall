package com.deko.mall.common.rabbitmq.service;

import com.deko.mall.common.rabbitmq.dto.MessageSendDTO;

/**
 * 消息发送服务
 * 提供普通消息和延迟消息的发送功能
 */
public interface MessageSendService {

    /**
     * 发送消息
     */
    void sendMessage(MessageSendDTO dto);

    /**
     * 发送延迟消息
     */
    void sendDelayMessage(MessageSendDTO dto);
}
