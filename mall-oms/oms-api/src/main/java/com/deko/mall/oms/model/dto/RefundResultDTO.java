package com.deko.mall.oms.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 退款回调结果
 */
@Data
@Accessors(chain = true)
public class RefundResultDTO {
    /**
     * 支付单号
     */
    private String payNo;

    /**
     * 退款单号
     */
    private String refundNo;

    /**
     * 申请单号（售后单号/取消订单号等）
     */
    private String applyNo;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 退款金额（元）
     */
    private BigDecimal refundAmount;

    /**
     * 退款状态
     */
    private Integer status;

    /**
     * 渠道退款单号
     */
    private String channelRefundNo;

    /**
     * 退款时间
     */
    private String refundTime;

    /**
     * 售后单ID
     * 当该字段非空时，表示这是一个售后退款
     */
    private Long afterSaleId;

    /**
     * 退款类型
     * 1 - 订单退款（取消订单等）
     * 2 - 售后退款
     */
    private Integer refundType;

    /**
     * 回调原始数据
     */
    private String notifyData;

}
