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
 * 业务退款单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pay_refund")
public class PayRefund extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 退款单号（业务唯一）
     */
    private String refundNo;

    /**
     * 申请单号（售后单号/取消订单号等）
     */
    private String applyNo;

    /**
     * 关联业务支付单号
     */
    private String paymentNo;

    /**
     * 关联订单号
     */
    private String orderNo;

    /**
     * 会员ID
     */
    private Long memberId;

    /**
     * 退款金额（单位：元）
     */
    private BigDecimal refundAmount;

    /**
     * 退款状态：0=请求,1=处理中,2=成功,3=失败
     */
    private Integer status;

    /**
     * 退款原因
     */
    private String reason;
    /** 逻辑删除 0=正常 1=删除 */
    @TableLogic
    private Integer deleted;

}
