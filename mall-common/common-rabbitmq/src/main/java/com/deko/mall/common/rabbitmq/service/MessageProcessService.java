package com.deko.mall.common.rabbitmq.service;

import com.deko.mall.common.rabbitmq.entity.MqMessageProcess;

/**
 * 消息处理服务
 * 提供消息处理记录的增删改查功能
 */
public interface MessageProcessService {

    /**
     * 检查消息是否已处理成功
     *
     * @param messageId     消息ID
     * @param consumerQueue 消费队列
     * @return true-已处理成功 false-未处理或处理失败
     */
    boolean isProcessed(String messageId, String consumerQueue);

    /**
     * 保存消息处理记录
     * 如果记录已存在则更新
     *
     * @param process 消息处理记录
     */
    void saveMessageProcess(MqMessageProcess process);

    /**
     * 更新消息处理记录
     * 如果记录不存在则新建
     * 支持更新：状态、重试次数、错误信息、消息体等
     *
     * @param process 消息处理记录
     */
    void updateMessageProcess(MqMessageProcess process);

    /**
     * 根据消息ID查询处理记录
     *
     * @param messageId 消息ID
     * @return 消息处理记录
     */
    MqMessageProcess findByMessageId(String messageId);

    /**
     * 根据业务ID删除消息处理记录（数据运维用）
     *
     * @param businessId 业务ID（订单号等）
     */
    void deleteByBusinessId(String businessId);
}
