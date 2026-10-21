package com.deko.mall.pay.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 支付响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseVO {

    /**
     * 支付单号
     */
    private String payNo;


    /**
     * 支付渠道交易单号
     */
    private String channelTradeNo;


    /**
     * 支付商户ID
     */
    private String mchId;

    /**
     * 支付参数(如微信支付参数、支付宝支付参数)
     */
    private String payParam;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 支付状态
     */
    private Integer payStatus;
}

