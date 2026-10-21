package com.deko.mall.pms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品视图对象
 */
@Data
@Schema(description = "商品视图对象")
public class PmsSpuRenderVO {

    @Schema(description = "SPU ID")
    private Long spuId;

    @Schema(description = "商品名称")
    private String title;

    @Schema(description = "商品主图")
    private String imageUrl;

    @Schema(description = "售价")
    private BigDecimal price;

    @Schema(description = "原价")
    private BigDecimal originalPrice;

    @Schema(description = "总销量")
    private Integer sales;

    @Schema(description = "总库存")
    private Integer stock;

    @Schema(description = "是否有库存")
    private Boolean hasStock;

    @Schema(description = "是否在售")
    private Boolean isOnSale;

    @Schema(description = "商品链接")
    private String link;
}

