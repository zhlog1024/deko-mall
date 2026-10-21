package com.deko.mall.pms.model.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * SKU信息DTO
 */
@Data
public class SkuInfoVO {

    /**
     * SKU ID
     */
    private Long id;

    /**
     * SKU 编号
     */
    private String skuSn;

    /**
     * SKU 名称
     */
    private String skuName;

    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * SPU ID
     */
    private Long spuId;

    /**
     * 商品名称
     */
    private String spuName;

    /**
     * 商品类型
     */
    private String spuType;
    private String spuTypeDesc;

    /**
     * 供货价(单位：元)
     */
    private BigDecimal supplyPrice;
    /**
     * 零售价(单位：元)
     */
    private BigDecimal retailPrice;

    /**
     * 市场价(单位：元)
     */
    private BigDecimal marketPrice;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 锁定库存数量
     */

    private Integer lockedStock;

    /**
     * 商品图片
     */
    private String picUrl;

    /**
     * 禁售区域ID
     */
    private Long forbiddenAreaId;

    /**
     * 运费模板ID
     */
    private Long freightTemplateId;

    /**
     * 是否需要发货
     */
    private Boolean needDelivery;
}
