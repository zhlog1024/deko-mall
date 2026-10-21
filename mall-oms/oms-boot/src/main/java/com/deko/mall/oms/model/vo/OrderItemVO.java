package com.deko.mall.oms.model.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单商品VO
 */
@Data
public class OrderItemVO {

    private Long id;
    private Long orderId;
    private String spuName;
    private Long skuId;
    private String skuSn;
    private String skuName;
    private String picUrl;
    private BigDecimal price;
    private BigDecimal retailPrice;
    private BigDecimal supplyPrice;
    private Integer quantity;
    private BigDecimal totalAmount;
    private BigDecimal productAmount;
    private Long freightTemplateId;
    private BigDecimal freightAmount;
    private Long forbiddenAreaId;
    private Long shopId;
    private String shopName;
    private Integer refundStatus;
    private Integer refundQuantity;
    private BigDecimal refundAmount;
    private Long supportId;
}
