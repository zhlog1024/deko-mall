package com.deko.mall.oms.model.entity.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情表
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OmsOrder extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 订单类型
     */
    private String orderType;

    /**
     * 订单场景: NORMAL-普通,
     * 
     */
    private String orderScene;

    /**
     * 订单总额（元）
     */
    private BigDecimal totalAmount;

    /**
     * 零售总价（元）
     */
    private BigDecimal totalRetailPrice;
    /**
     * 供货总价（元）- 冗余商品供货价
     */
    private BigDecimal totalSupplyPrice;

    /**
     * 支付商户编号
     */
    private String mchId;

    /**
     * 商品总数
     */
    private Integer totalQuantity;

    /**
     * 订单来源(0-PC订单；1-app订单)
     */
    private Integer source;

    /**
     * 订单状态(1-待付款;2-待发货;3-已发货;4-已完成;5-已关闭;6-已取消;)
     */
    private Integer status;

    /**
     * 订单备注
     */
    private String remark;

    /**
     * 会员id
     */
    private Long memberId;

    /**
     * 下单人手机号（快照，下单时从 JWT 冗余）
     */
    private String buyerPhone;

    /**
     * 使用的优惠券
     */
    private Long couponId;

    /**
     * 优惠券抵扣金额（元）
     */
    private BigDecimal couponAmount;

    /**
     * 运费金额（元）
     */
    private BigDecimal freightAmount;

    /**
     * 使用的积分金额
     */
    private BigDecimal usePointsAmount;

    /**
     * 积分抵扣金额（元）
     */
    private BigDecimal pointsDeductionAmount;

    /**
     * 支付抵扣金额（元）
     */
    private BigDecimal paymentDiscountAmount;

    /**
     * 应付总额（元）
     */
    private BigDecimal paymentAmount;

    /**
     * 支付时间
     */
    private LocalDateTime paymentTime;

    /**
     * 支付方式【1->微信jsapi；2->支付宝；3->余额；4->微信app；】
     */
    private String payChannel;

    /**
     * 业务支付单号
     * 关联的业务层支付单号 (oms_payment.payment_no)
     */
    private String paymentNo;

    /**
     * 支付申请号（渠道请求号）
     * 支付服务生成的渠道请求号 (pay_payment_channel.payment_channel_no)
     */
    private String paymentChannelNo;
    /**
     * 渠道交易号
     * 第三方支付平台返回的交易流水号（如微信transaction_id、支付宝trade_no），用于对账
     */
    private String channelTradeNo;

    /**
     * 渠道用户标识（微信 openid 等），用于发货报备
     */
    private String channelUserId;

    /**
     * 发货时间
     */
    private LocalDateTime deliveryTime;

    /**
     * 确认收货时间
     */
    private LocalDateTime receiveTime;

    /**
     * 分享用户ID
     */
    private Long shareUserId;

    /**
     * 店铺ID(下单快照，用户侧按店铺维度展示/售后)
     */
    private Long shopId;

    /**
     * 店铺名称(下单快照，店铺改名不影响历史订单)
     */
    private String shopName;

    /**
     * 逻辑删除标识(1:已删除；0:正常)
     */
    private Integer deleted;

    /**
     * 订单商品列表
     */
    @TableField(exist = false)
    private List<OmsOrderItem> orderItems;

    /**
     * 取消时间
     */
    private LocalDateTime cancelTime;

    /**
     * 退款时间
     */
    private LocalDateTime refundTime;

    /**
     * 是否需要发货：0-不需要 1-需要
     */
    private Boolean needDelivery;

}
