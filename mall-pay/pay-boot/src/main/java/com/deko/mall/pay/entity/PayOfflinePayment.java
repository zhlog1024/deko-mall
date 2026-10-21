package com.deko.mall.pay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 线下支付凭证审核实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pay_offline_payment")
public class PayOfflinePayment extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 支付单号（关联 pay_payment.payment_no）
     */
    private String paymentNo;

    /**
     * 渠道支付请求号（关联 pay_payment_channel.payment_channel_no）
     */
    private String paymentChannelNo;

    /**
     * 支付凭证图片URL，JSON数组格式
     */
    private String voucherUrls;

    /**
     * 审核状态: 0=待审核, 1=审核通过, 2=审核驳回
     */
    private Integer auditStatus;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 审核人用户名
     */
    private String auditor;

    /**
     * 重新提交次数
     */
    private Integer resubmitCount;

    /**
     * 逻辑删除 0=正常 1=删除
     */
    @TableLogic
    private Integer deleted;
}
