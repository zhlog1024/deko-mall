package com.deko.mall.oms.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情视图对象
 */
@Schema(description = "订单详情视图对象")
@Data
public class OmsOrderDetailVO {

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "支付单号")
    private String paymentNo;

    @Schema(description = "订单类型")
    private String orderType;

    @Schema(description = "订单类型标签")
    private String orderTypeLabel;

    @Schema(description = "订单总金额(元)")
    private BigDecimal totalAmount;

    @Schema(description = "零售总价(元)")
    private BigDecimal totalRetailPrice;
    @Schema(description = "供货总价(元)")
    private BigDecimal totalSupplyPrice;

    @Schema(description = "运费金额(元)")
    private BigDecimal freightAmount;

    @Schema(description = "优惠券抵扣金额(元)")
    private BigDecimal couponAmount;

    @Schema(description = "支付时间")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime paymentTime;

    @Schema(description = "应付金额(元)")
    private BigDecimal paymentAmount;

    @Schema(description = "支付方式")
    private String payChannel;

    @Schema(description = "支付方式标签")
    private String payChannelLabel;

    @Schema(description = "订单状态")
    private Integer status;

    @Schema(description = "订单状态标签")
    private String statusLabel;

    @Schema(description = "商品总数")
    private Integer totalQuantity;

    @Schema(description = "订单创建时间")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "订单来源标签")
    private String sourceLabel;

    @Schema(description = "订单备注")
    private String remark;

    @Schema(description = "会员id")
    private Long memberId;

    @Schema(description = "店铺id")
    private Long shopId;

    @Schema(description = "店铺名称")
    private String shopName;

    @Schema(description = "订单商品集合")
    private List<OrderItemVO> orderItems;

    @Schema(description = "物流单号")
    private String trackingNo;

    @Schema(description = "物流公司")
    private String carrierName;

    @Schema(description = "发货时间")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime deliveryTime;

    @Schema(description = "发货类型：1线上物流 2线下发货")
    private Integer deliveryType;

    @Schema(description = "线下发货备注")
    private String offlineRemark;

    @Schema(description = "收件人姓名")
    private String receiverName;

    @Schema(description = "收件人电话")
    private String receiverPhone;

    @Schema(description = "收件人省份")
    private String receiverProvince;

    @Schema(description = "收件人城市")
    private String receiverCity;

    @Schema(description = "收件人区县")
    private String receiverDistrict;

    @Schema(description = "收件人详细地址")
    private String receiverAddress;

    @Schema(description = "收件人完整地址")
    private String receiverCompleteAddress;

    @Schema(description = "下单人手机号")
    private String buyerPhone;

    /**
     * 是否需要发货：0-不需要 1-需要
     */
    @Schema(description = "是否需要发货")
    private Boolean needDelivery;

    @Schema(description = "使用的积分金额(元)")
    private BigDecimal usePointsAmount;

    @Schema(description = "积分抵扣金额(元)")
    private BigDecimal pointsDeductionAmount;

    @Schema(description = "支付抵扣金额(元)")
    private BigDecimal paymentDiscountAmount;

    @Schema(description = "退款金额 (元)")
    private BigDecimal refundAmount;

    @Schema(description = "确认收货时间")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime receiveTime;

    @Schema(description = "货款结算时间")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime settlementTime;

    @Schema(description = "线下支付凭证图片列表")
    private List<String> offlinePayVouchers;

    @Schema(description = "线下支付审核状态: 0待审核, 1通过, 2驳回")
    private Integer offlinePayAuditStatus;

    @Schema(description = "线下支付审核备注")
    private String offlinePayAuditRemark;

    @Schema(description = "线下支付审核时间")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime offlinePayAuditTime;

    @Schema(description = "线下支付审核人")
    private String offlinePayAuditor;

    /**
     * 订单商品明细
     */
    @Schema(description = "订单商品明细")
    @Data
    public static class OrderItemVO {

        @Schema(description = "订单商品ID")
        private Long id;

        @Schema(description = "店铺ID")
        private Long shopId;

        @Schema(description = "店铺名称")
        private String shopName;

        @Schema(description = "商品ID")
        private Long spuId;

        @Schema(description = "商品名称")
        private String spuName;

        @Schema(description = "规格ID")
        private Long skuId;

        @Schema(description = "SKU编号")
        private String skuSn;

        @Schema(description = "商品规格名称")
        private String skuName;

        @Schema(description = "图片地址")
        private String picUrl;

        @Schema(description = "商品单价")
        private BigDecimal price;

        @Schema(description = "零售价")
        private BigDecimal retailPrice;
        @Schema(description = "供货价")
        private BigDecimal supplyPrice;

        @Schema(description = "商品数量")
        private Integer quantity;

        @Schema(description = "商品总金额(单位：元)")
        private BigDecimal totalAmount;

        @Schema(description = "运费金额(单位：元)")
        private BigDecimal freightAmount;

        @Schema(description = "履约状态: 0取消/1待发货/2已发货/3已收货/4售后中/5已退款")
        private Integer status;

        @Schema(description = "退款状态: 0正常/1全额退款/2部分退款")
        private Integer refundStatus;

        @Schema(description = "退款数量")
        private Integer refundQuantity;

        @Schema(description = "退款金额(元)")
        private BigDecimal refundAmount;

    }

}
