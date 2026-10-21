package com.deko.mall.pay.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 支付退款DTO
 * 用于统一支付系统和订单系统之间的退款请求数据传输
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequestDTO {

    /**
     * 退款单号
     */
    private String refundNo;

    /**
     * 申请单号（售后单号/取消订单号等）
     */
    private String applyNo;

    /**
     * 退款类型
     */
    private Integer refundType;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 支付单号
     */
    private String paymentNo;

    /**
     * 退款金额(元)
     */
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    private String refundReason;


}
