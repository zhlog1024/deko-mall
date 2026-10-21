package com.deko.mall.oms.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 购物车商品项
 */
@Data
public class CartItemDto implements Serializable {

    /**
     * 商品库存ID
     */
    private Long skuId;

    /**
     * 商品数量
     */
    private Integer count;

    /**
     * 是否选中
     */
    private Boolean checked;

    /**
     * SKU 编号
     */
    private String skuSn;
    /**
     * SKU 名称
     */
    private String skuName;
    /**
     * SKU 图片地址
     */
    private String picUrl;
    /**
     * SKU 价格
     */
    private BigDecimal price;
    /**
     * SKU 库存数量
     */
    private Integer stock;
    /**
     * 所属SPU的ID
     */
    private Long spuId;

    /**
     * 所属SPU的名称
     */
    private String spuName;

    /**
     * 供应商ID（下单分组内部逻辑使用）
     */
    private Long supplierId;

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
     * 运费模板ID
     */
    private Long freightTemplateId;


}
