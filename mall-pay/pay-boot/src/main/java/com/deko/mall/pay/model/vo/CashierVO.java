package com.deko.mall.pay.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 收银台展示对象
 */
@Data
public class CashierVO {

    /**
     * 支付单号
     */
    private String paymentNo;

    /**
     * 支付金额(元)
     */
    private BigDecimal payAmount;

    /**
     * 原始支付金额（元），未应用支付折扣前的金额
     */
    private BigDecimal originalAmount;

    /**
     * 支付抵扣金额（元），实际应用的优惠/立减
     */
    private BigDecimal paymentDiscountAmount;

    /**
     * 可用支付渠道列表
     */
    private List<PayChannelVO> channels;
}
