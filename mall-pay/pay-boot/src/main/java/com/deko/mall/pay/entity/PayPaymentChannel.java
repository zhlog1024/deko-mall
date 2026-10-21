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
 * 渠道支付请求（原 oms_pay_payment 调整为技术请求）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pay_payment_channel")
public class PayPaymentChannel extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 渠道请求号（原 payNo）
     */
    private String paymentChannelNo;

    /**
     * 关联业务支付单号
     */
    private String paymentNo;

    /**
     * 会员ID（从业务支付单冗余）
     */
    private Long memberId;
    private String paymentChannel;
    private String mchId;
    private String paymentContent;

    /**
     * 支付金额（元）
     */
    private BigDecimal amount;

    /**
     * 渠道请求状态：0=待支付,1=支付中,2=成功,3=失败,4=关闭
     */
    private Integer status;

    private String channelTradeNo;
    private String channelUserId;
    /**
     * 客户端IP
     */
    private String clientIp;
    /** 设备类型（H5/MINI/APP），用于策略内部路由 */
    private String deviceType;
    private String payParam;
    private String notifyParam;
    private LocalDateTime expireTime;
    /** 逻辑删除 0=正常 1=删除 */
    @TableLogic
    private Integer deleted;

}
