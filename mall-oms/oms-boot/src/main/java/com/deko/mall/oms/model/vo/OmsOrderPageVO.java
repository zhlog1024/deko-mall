package com.deko.mall.oms.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Admin-订单分页视图对象
 */
@Schema(description = "Admin-订单分页视图对象")
@Data
public class OmsOrderPageVO {

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "订单类型")
    private String orderType;

    @Schema(description = "订单类型标签")
    private String orderTypeLabel;

    @Schema(description = "订单场景")
    private String orderScene;


    @Schema(description = "订单总金额(元)")
    private BigDecimal totalAmount;


    @Schema(description = "运费金额(元)")
    private BigDecimal freightAmount;

    @Schema(description = "支付时间")
    private LocalDateTime paymentTime;


    @Schema(description = "订单总金额(元)")
    private BigDecimal paymentAmount;

    @Schema(description = "支付方式")
    private String payChannel;

    @Schema(description = "支付方式标签")
    private String payChannelLabel;

    @Schema(description = "支付申请号（渠道请求号）")
    private String paymentChannelNo;

    @Schema(description = "渠道交易号，用于对账")
    private String channelTradeNo;

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


    @Schema(description = "订单商品集合")
    private List<OrderItem> orderItems;

    @Schema(description = "物流单号")
    private String trackingNo;

    @Schema(description = "物流公司")
    private String carrierName;

    @Schema(description = "发货类型：1线上物流 2线下发货")
    private Integer deliveryType;

    @Schema(description = "线下发货备注")
    private String offlineRemark;






    @Schema(description = "线下发货凭证")
    private String offlineProof;

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


    /**
     * 佣金结算状态(0-未结算, 1-已结算)
     */
    @Schema(description = "积分抵扣金额(元)")
    private BigDecimal pointsDeductionAmount;





    @Schema(description = "订单商品明细")
    @Data
    public static class OrderItem {

        @Schema(description = "订单商品ID")
        private Long id;

        @Schema(description = "商品ID")
        private Long spuId;

        @Schema(description = "规格ID")
        private Long skuId;

        @Schema(description = "商品规格名称")
        private String skuName;

        @Schema(description = "商品编码")
        private String skuSn;

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

}
