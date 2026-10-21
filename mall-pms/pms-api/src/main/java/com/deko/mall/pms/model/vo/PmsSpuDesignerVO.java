package com.deko.mall.pms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 装修器商品视图对象
 * 统一字段格式，与指定商品接口(PmsSpuDTO/SpuPageVO)保持一致
 */
@Schema(description = "装修器商品视图对象")
@Data
public class PmsSpuDesignerVO {

    @Schema(description = "商品ID")
    private Long id;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "商品主图")
    private String picUrl;

    @Schema(description = "售价")
    private BigDecimal price;

    @Schema(description = "原价")
    private BigDecimal originalPrice;

    @Schema(description = "销量")
    private Integer sales;

    @Schema(description = "总库存")
    private Integer stock;

    @Schema(description = "商品类型")
    private String spuType;
}
