package com.deko.mall.pay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 渠道退款请求（技术层）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pay_refund_channel")
public class PayRefundChannel extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 渠道退款单号
     */
    private String refundChannelNo;

    /**
     * 关联退款单号
     */
    private String refundNo;

    /**
     * 关联支付单号
     */
    private String paymentNo;

    /**
     * 关联渠道支付单号
     */
    private String paymentChannelNo;

    /**
     * 支付渠道
     */
    private String paymentChannel;

    /**
     * 退款金额（单位：元）
     */
    private BigDecimal amount;

    /**
     * 退款状态
     */
    private Integer status;

    /**
     * 渠道退款流水号（第三方返回）
     */
    private String channelTradeNo;

    /**
     * 请求参数
     */
    private String param;

    /**
     * 响应结果
     */
    private String result;
    /** 逻辑删除 0=正常 1=删除 */
    @TableLogic
    private Integer deleted;

}
