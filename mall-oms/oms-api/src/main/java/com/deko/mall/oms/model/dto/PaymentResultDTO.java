package com.deko.mall.oms.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 支付回调结果
 */
@Data
@Accessors(chain = true)
public class PaymentResultDTO {
    /**
     * 支付渠道号（渠道请求号）
     */
    private String paymentChannelNo;

    /**
     * 业务支付单号
     */
    private String paymentNo;

    /**
     * 渠道交易号（第三方支付平台的交易号）
     */
    private String channelTradeNo;

    /**
     * 支付状态
     */
    private Integer status;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 回调原始数据
     */
    private String notifyData;

    /**
     * 支付渠道
     */
    private String payChannel;

    /**
     * 商户ID
     */
    private String mchId;
}
