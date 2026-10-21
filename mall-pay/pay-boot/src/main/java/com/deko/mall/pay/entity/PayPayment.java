package com.deko.mall.pay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 业务支付单（支付单）实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pay_payment")
public class PayPayment extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 支付单号（业务层唯一）
     */
    private String paymentNo;

    /**
     * 业务类型，如 ORDER/RECHARGE
     */
    private String businessType;

    /**
     * 会员ID
     */
    private Long memberId;

    /**
     * 支付金额（元）
     */
    private BigDecimal amount;

    /**
     * 原始支付金额（元），未应用支付折扣前的金额，用于收银台展示
     */
    private BigDecimal originalPaymentAmount;

    /**
     * 支付抵扣金额（元），支付单上实际生效的优惠/立减
     */
    private BigDecimal paymentDiscountAmount;

    /**
     * 支付单状态：0=待支付,1=支付中,2=已支付,3=失败,4=取消
     */
    private Integer status;

    /**
     * 最终支付渠道（支付成功后写入）
     */
    private String paymentChannel;

    /**
     * 支付完成时间
     */
    private LocalDateTime paidTime;

    /**
     * 支付展示内容（商品名称摘要）
     */
    private String paymentContent;

    /**
     * 支付扩展配置（JSON格式），用于控制支付方式展示等
     */
    private String extConfig;
    /** 逻辑删除 0=正常 1=删除 */
    @TableLogic
    private Integer deleted;

}
