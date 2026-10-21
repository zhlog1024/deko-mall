package com.deko.mall.oms.constant;

/**
 * 订单相关常量
 *
 * <p>该接口定义了与订单相关的常量。</p>
 */
public interface OrderConstants {

    /**
     * 会员购物车缓存键前缀
     */
    String MEMBER_CART_PREFIX = "order:cart:";

    /**
     * 订单防重提交令牌缓存键前缀
     */
    String ORDER_TOKEN_PREFIX = "order:token:";

    /**
     * 订单令牌/跨服务下单幂等键的有效期（分钟）
     */
    long ORDER_TOKEN_TTL_MINUTES = 30L;

    /**
     * 订单锁缓存键前缀
     */
    String ORDER_LOCK_PREFIX = "order:lock";

    /**
     * 订单相关消息交换机
     */
    String ORDER_EXCHANGE = "order.topic.exchange";

    String ORDER_AFTERSALE_AUDIT_ROUTING_KEY = "order.aftersale.audit";

}
