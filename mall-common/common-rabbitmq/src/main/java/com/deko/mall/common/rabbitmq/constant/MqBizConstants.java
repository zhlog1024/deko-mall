package com.deko.mall.common.rabbitmq.constant;

/**
 * 订单模块消息队列常量
 */
public interface MqBizConstants {


    /**
     * 订单支付消息
     */
    interface MessageSource {
        String OMS_SERVICE = "OMS_SERVICE";
        String UMS_SERVICE = "UMS_SERVICE";
        String PMS_SERVICE = "PMS_SERVICE";
        String SYSTEM_SERVICE = "SYSTEM_SERVICE";
    }


    /**
     * 订单退款回调消息
     */
    interface MessageType {
        String PAYMENT_RESULT = "PAYMENT_RESULT";
        String REFUND_REQUEST = "REFUND_REQUEST";
        String REFUND_RESULT = "REFUND_RESULT";
        String SETTLEMENT_RESULT = "SETTLEMENT_RESULT";
        String SYSTEM_NOTIFY = "SYSTEM_NOTIFY";
    }

    /**
     * 订单支付消息
     */
    interface PaymentResult {
        String EXCHANGE = "payment.result.exchange";
        String QUEUE = "payment.result.queue";
        String ROUTING_KEY = "payment.result";
    }




    /**
     * 退款响应
     */
    interface RefundResult {
        String EXCHANGE = "refund.result.exchange";
        String QUEUE = "refund.result.queue";
        String ROUTING_KEY = "refund.result";
    }

    /**
     * 退款请求
     */
    interface RefundRequest {
        String EXCHANGE = "refund.request.exchange";
        String QUEUE = "refund.request.queue";
        String ROUTING_KEY = "refund.request";
    }

    /**
     * 系统通知消息（企业微信、短信等消息推送）
     */
    interface SystemNotify {
        String EXCHANGE = "system.notify.exchange";
        String QUEUE = "system.notify.queue";
        String ROUTING_KEY = "system.notify";
    }


}
