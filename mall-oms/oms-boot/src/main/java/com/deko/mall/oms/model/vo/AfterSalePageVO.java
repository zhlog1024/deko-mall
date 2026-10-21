package com.deko.mall.oms.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 售后详情视图对象
 */
@Data
@Schema(description = "售后详情视图对象")
public class AfterSalePageVO {

    @Schema(description = "售后ID")
    private Long id;

    @Schema(description = "售后单号")
    private String afterSaleNo;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "售后类型(1-仅退款，2-退货退款)")
    private Integer afterSaleType;

    @Schema(description = "售后类型文本")
    private String afterSaleTypeLabel;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "店铺ID")
    private Long shopId;

    @Schema(description = "店铺名称")
    private String shopName;

    private Long supplierId;

    private String supplierName;

    @Schema(description = "申请原因")
    private String reason;

    @Schema(description = "问题描述")
    private String description;

    @Schema(description = "凭证图片，多个以逗号分隔")
    private String applyPics;


    @Schema(description = "退款金额(元)")
    private BigDecimal refundAmount;

    @Schema(description = "使用的积分金额(元)")
    private BigDecimal usePointsAmount;


    @Schema(description = "售后状态")
    private Integer status;

    @Schema(description = "售后状态文本")
    private String statusLabel;

    @Schema(description = "处理人")
    private String handleBy;

    @Schema(description = "处理时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime handleTime;

    @Schema(description = "处理备注")
    private String handleNote;

    @Schema(description = "退货物流单号")
    private String trackingNo;

    @Schema(description = "退货物流公司编码")
    private String carrierCode;

    @Schema(description = "退货物流公司")
    private String carrierName;

    @Schema(description = "发货人手机号")
    private String senderPhone;


    @Schema(description = "退货凭证图片，多个以逗号分隔")
    private String returnPics;

    @Schema(description = "退货时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime returnTime;

    @Schema(description = "收货时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime receiveTime;

    @Schema(description = "退款时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime refundTime;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(description = "售后商品列表")
    private List<OmsOrderAfterSaleItemVO> items;

    @Schema(description = "退货地址（退货退款审批通过后返回）")
    private ReturnAddress returnAddress;

    @Data
    @Schema(description = "退货地址")
    public static class ReturnAddress {
        @Schema(description = "收货人")
        private String name;
        @Schema(description = "联系电话")
        private String tel;
        @Schema(description = "详细地址")
        private String address;
    }
}
