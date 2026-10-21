package com.deko.mall.ums.model.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 收藏视图对象（商品/店铺通用）
 */
@Data
public class FavorVO {

    /**
     * 收藏记录ID
     */
    private Long favorId;

    /**
     * 对象类型(GOODS-商品, SHOP-店铺)
     */
    private String targetType;

    /**
     * 对象ID（商品ID或店铺ID）
     */
    private Long targetId;

    /**
     * 对象名称
     */
    private String name;

    /**
     * 商品价格
     */
    private BigDecimal price;

    /**
     * 商品原价
     */
    private BigDecimal originalPrice;

    /**
     * 商品图片
     */
    private String picUrl;

    /**
     * 商品单位
     */
    private String unit;

    /**
     * 店铺logo
     */
    private String logo;

    /**
     * 店铺简介
     */
    private String description;

    /**
     * 店铺状态(0-关闭,1-开启)
     */
    private Integer status;
}
