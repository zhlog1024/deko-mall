package com.deko.mall.oms.model.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单商品DTO
 */
@Data
public class OrderItemDTO {

    /**
     * 订单项ID
     */
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 商品名称
     */
    private String spuName;

    /**
     * 商品SPU ID
     */
    private Long spuId;

    /**
     * 规格ID
     */
    private Long skuId;

    /**
     * SKU编码
     */
    private String skuSn;

    /**
     * 规格名称
     */
    private String skuName;

    /**
     * 商品图片地址
     */
    private String picUrl;

    /**
     * 商品单价(单位：元)
     */
    private BigDecimal price;

    /**
     * 零售价
     */
    private BigDecimal retailPrice;

    /**
     * 供货价
     */
    private BigDecimal supplyPrice;
    /**
     * 商品数量
     */
    private Integer quantity;

    /**
     * 商品总金额(单位：元)
     */
    private BigDecimal totalAmount;

    /**
     * 商品行金额(兼容旧版)
     */
    private BigDecimal productAmount;

    /**
     * 运费模板ID
     */
    private Long freightTemplateId;

    /**
     * 运费金额(单位：元)
     */
    private BigDecimal freightAmount;

    /**
     * 禁售区域ID
     */
    private Long forbiddenAreaId;

    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 供应商名称
     */
    private String supplierName;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 商品类型（NORMAL）
     */
    private String spuType;

    /**
     * 售后状态（0：正常，1：售后中，2：已退款，3：退款失败）
     */
    private Integer refundStatus;

    /**
     * 退款数量
     */
    private Integer refundQuantity;

    /**
     * 退款金额(单位：元)
     */
    private BigDecimal refundAmount;

    /**
     * 关联售后单ID
     */
    private Long supportId;
}
