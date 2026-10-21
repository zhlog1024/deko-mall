package com.deko.mall.oms.model.bo;

import com.deko.mall.common.base.BaseEntity;
import com.deko.mall.oms.enums.OrderSourceEnum;
import com.deko.mall.oms.enums.OrderStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单业务对象
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OrderBO extends BaseEntity {

    /**
     * 订单ID
     */
    private Long id;
    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 业务支付单号（payment_no）
     */
    private String paymentNo;

    /**
     * 支付申请号（渠道请求号）
     */
    private String paymentChannelNo;

    /**
     * 渠道交易号
     * 第三方支付平台返回的交易流水号，用于对账
     */
    private String channelTradeNo;

    /**
     * 订单类型
     */
    private String orderType;

    private String orderScene;
    /**
     * 订单总额（元）
     */
    private BigDecimal totalAmount;
    /**
     * 商品总数
     */
    private Integer totalQuantity;

    /**
     * 订单来源 {@link OrderSourceEnum}
     */
    private Integer source;

    /**
     * 订单状态 {@link OrderStatusEnum}
     */
    private Integer status;

    /**
     * 应付总额（元）
     */
    private BigDecimal paymentAmount;


    /**
     * 运费金额（元）
     */
    private BigDecimal freightAmount;

    /**
     * 支付方式
     */
    private String payChannel;


    private LocalDateTime paymentTime;


    /**
     * 发货时间
     */
    private LocalDateTime deliveryTime;


    private LocalDateTime receiveTime;


    private LocalDateTime cancelTime;


    private LocalDateTime settlementTime;

    /**
     * 商户ID
     */
    private String mchId;

    /**
     * 订单创建时间
     */
    private LocalDateTime createTime;

    private Long memberId;



    private Long shopId;

    private String shopName;

    private String shopLogo;






    /**
     * 订单备注
     */
    private String remark;

    /**
     * 是否需要发货：0-不需要 1-需要
     */
    private Boolean needDelivery;

    /**
     * 收件人姓名
     */
    private String receiverName;

    /**
     * 收件人电话
     */
    private String receiverPhone;

    /**
     * 收件人省份
     */
    private String receiverProvince;

    /**
     * 收件人城市
     */
    private String receiverCity;

    /**
     * 收件人区县
     */
    private String receiverDistrict;

    /**
     * 收件人详细地址
     */
    private String receiverAddress;

    /**
     * 收件人完整地址
     */
    private String receiverCompleteAddress;

    /**
     * 下单人手机号
     */
    private String buyerPhone;

    /**
     * 物流公司
     */
    private String carrierName;

    /**
     * 物流单号
     */
    private String trackingNo;

    /**
     * 使用的积分金额（元）
     */
    private BigDecimal usePointsAmount;

    /**
     * 积分抵扣金额（元）
     */
    private BigDecimal pointsDeductionAmount;





    /**
     * 优惠券ID
     */
    private Long couponId;

    /**
     * 优惠券抵扣金额（元）
     */
    private BigDecimal couponAmount;

    /**
     * 订单商品明细列表
     */
    private List<OrderItem> orderItems;

    @Data
    public static class OrderItem {

        private Long id;

        /**
         * 供应商ID
         */
        private Long supplierId;

        /**
         * 订单ID
         */
        private Long orderId;


        private Long spuId;

        /**
         * 规格ID
         */
        private Long skuId;

        /**
         * SKU编号
         */
        private String skuSn;

        /**
         * 商品名称
         */
        private String skuName;

        /**
         * 商品sku图片
         */
        private String picUrl;

        /**
         * 商品单价(单位：元)
         */
        private BigDecimal price;

        /**
         * 商品数量
         */
        private Integer quantity;

        /**
         * 商品总金额(单位：元)(单价*数量)
         */
        private BigDecimal totalAmount;

        /**
         * 供货价(单位：元)
         */
        private BigDecimal supplyPrice;

        /**
         * 零售价(单位：元)
         */
        private BigDecimal retailPrice;

        private Integer isCommented;

        /**
         * 履约状态(0取消/1待发货/2已发货/3已收货/4售后中/5已退款)
         */
        private Integer status;




    }

}
