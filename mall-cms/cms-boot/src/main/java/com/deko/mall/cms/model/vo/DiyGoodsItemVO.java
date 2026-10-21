package com.deko.mall.cms.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DIY 装修器 - 商品列表项 VO
 */
@Data
@Schema(description = "DIY装修器商品列表项")
public class DiyGoodsItemVO {

    @Schema(description = "商品ID")
    private Long id;
    @Schema(description = "商品名称")
    private String name;
    @Schema(description = "商品主图")
    @JsonProperty("picUrl")
    private String picUrl;
    @Schema(description = "售价")
    private BigDecimal price;

    @Schema(description = "原价")
    @JsonProperty("originPrice")
    private BigDecimal originPrice;

    @Schema(description = "销量")
    private Integer sales;
    @Schema(description = "商品类型")
    @JsonProperty("spuType")
    private String spuType;
    @Schema(description = "商品类型描述")
    @JsonProperty("spuTypeDesc")
    private String spuTypeDesc;
    @Schema(description = "分类名称")
    @JsonProperty("categoryName")
    private String categoryName;
    @Schema(description = "品牌名称")
    @JsonProperty("brandName")
    private String brandName;
    @Schema(description = "商品简述/简介")
    @JsonProperty("simpleDesc")
    private String simpleDesc;
}
