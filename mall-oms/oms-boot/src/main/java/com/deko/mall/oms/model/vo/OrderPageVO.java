package com.deko.mall.oms.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * App-订单分页视图对象
 */
@Schema(description = "App-订单分页视图对象")
@Data
public class OrderPageVO {

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "业务支付单号")
    private String paymentNo;

    @Schema(description = "订单类型")
    private String orderType;

    @Schema(description = "订单类型标签")
    private String orderTypeLabel;


    @Schema(description = "订单总金额(元)")
    private BigDecimal totalAmount;


    @Schema(description = "运费金额(元)")
    private BigDecimal freightAmount;

    @Schema(description = "订单总金额(元)")
    private BigDecimal paymentAmount;

    @Schema(description = "支付方式")
    private String payChannel;

    @Schema(description = "支付方式标签")
    private String payChannelLabel;

    @Schema(description = "线下支付审核状态: 0待审核, 1通过, 2驳回")
    private Integer offlinePayAuditStatus;

    @Schema(description = "线下支付审核备注/驳回原因")
    private String offlinePayAuditRemark;

    @Schema(description = "订单状态")
    private Integer status;

    @Schema(description = "订单状态标签")
    private String statusLabel;


    @Schema(description = "是否可以申请售后(true-可以申请, false-不可申请)")
    private Boolean canApplyAfterSale;

    @Schema(description = "可用的售后类型列表(1-仅退款, 2-退货退款)")
    private List<Integer> availableAfterSaleTypes;

    @Schema(description = "是否可以快速退款(true-可以快速退款, false-不可快速退款)")
    private Boolean canQuickRefund;

    @Schema(description = "商品总数")
    private Integer totalQuantity;

    @Schema(description = "订单创建时间")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "订单支付时间")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime paymentTime;

    @Schema(description = "订单发货时间")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime deliveryTime;

    @Schema(description = "订单收货时间")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime receiveTime;


    @Schema(description = "订单取消时间")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime cancelTime;


    @Schema(description = "订单来源标签")
    private String sourceLabel;

    @Schema(description = "订单备注")
    private String remark;

    @Schema(description = "店铺ID")
    private Long shopId;

    @Schema(description = "店铺名称")
    private String shopName;







    @Schema(description = "店铺logo")
    private String shopLogo;

    // 收货人信息
    @Schema(description = "收货人姓名")
    private String receiverName;

    @Schema(description = "收货人电话")
    private String receiverPhone;

    @Schema(description = "收货地址")
    private String receiverAddress;

    @Schema(description = "收货地址省份")
    private String receiverProvince;

    @Schema(description = "收货地址城市")
    private String receiverCity;

    @Schema(description = "收货地址区县")
    private String receiverDistrict;

    @Schema(description = "收货邮编")
    private String receiverZipCode;

    // 物流信息
    @Schema(description = "物流公司名称")
    private String carrierName;

    @Schema(description = "物流单号")
    private String trackingNo;

    @Schema(description = "发货类型：1线上物流 2线下发货")
    private Integer deliveryType;

    @Schema(description = "线下发货备注")
    private String offlineRemark;

    @Schema(description = "线下发货凭证")
    private String offlineProof;

    @Schema(description = "订单商品集合")
    private List<OmsOrderPageVO.OrderItem> orderItems;

    @Schema(description = "使用的积分金额(元)")
    private BigDecimal usePointsAmount;


    @Schema(description = "积分抵扣金额(单位：元)")
    private BigDecimal pointsDeductionAmount;





    @Schema(description = "订单商品明细")
    @Data
    public static class OrderItem {

        @Schema(description = "订单商品ID")
        private Long id;

        @Schema(description = "商品SPU_ID")
        private Long spuId;

        @Schema(description = "商品SKU_ID")
        private Long skuId;

        @Schema(description = "商品规格名称")
        private String skuName;

        @Schema(description = "商品名称")
        private String spuName;

        @Schema(description = "图片地址")
        private String picUrl;


        @Schema(description = "商品价格")
        private BigDecimal price;

        @Schema(description = "商品数量")
        private Integer quantity;

        @Schema(description = "商品总金额(单位：元)")
        private BigDecimal totalAmount;

        @Schema(description = "是否已评论(0-否, 1-是)")
        private Integer isCommented;

    }

    @Schema(description = "是否可以评论(true-可以评论, false-不可评论)")
    private Boolean canComment;

}
