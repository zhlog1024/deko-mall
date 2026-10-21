package com.deko.mall.ums.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 商品实体
 */
@Data
@Accessors(chain = true)
public class SmsSpuVO {

    private Long id;

    private String name;

    private Long categoryId;

    private Long brandId;

    private BigDecimal price;

    private Integer sales;

    private String picUrl;

    private String[] album;

    private String unit;

    private String description;

    private String detail;

    private String spuType;

    /**
     * 商品类型描述（普通商品）
     */
    private String spuTypeDesc;

    private Integer status;

}

