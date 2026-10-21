package com.deko.mall.pay.api.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付请求参数
 */
@Data
public class PaymentRequest {

    /** 业务支付单号 */
    private String paymentNo;

    /** 支付渠道（PayChannelEnum） */
    private String payChannel;

    /** 支付金额 */
    private BigDecimal payAmount;

    /** 设备类型（H5/MINI/APP） */
    private String deviceType;

    /** 用户ID */
    private Long memberId;

    /** 微信授权码/小程序code */
    private String code;

    /** 微信openId */
    private String openId;

    /** 商品描述 */
    private String body;

    /** 支付截止时间 */
    private String expireTime;

    /** 客户端IP */
    private String clientIp;

    /** 支付配置扩展信息JSON */
    private String extConfig;

    /** 订单号列表（多个订单合并支付） */
    private String orderNos;

    /**
     * 业务类型（ORDER/PURCHASE…）
     * <p>支付单是业务无关的：不同业务方创建支付单时声明自己的类型，便于对账与按业务订阅支付结果。
     * 不传时默认 {@code ORDER}（兼容既有调用方）。
     */
    private String businessType;
}
