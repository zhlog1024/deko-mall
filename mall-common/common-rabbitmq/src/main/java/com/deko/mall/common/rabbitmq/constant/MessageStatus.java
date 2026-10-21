package com.deko.mall.common.rabbitmq.constant;

/**
 * 消息状态
 */
public interface MessageStatus {

    /**
     * 待处理
     */
    String PENDING = "PENDING";

    /**
     * 处理中
     */
    String PROCESSING = "PROCESSING";

    /**
     * 处理成功
     */
    String SUCCESS = "SUCCESS";

    /**
     * 处理失败
     */
    String FAILED = "FAILED";

}
