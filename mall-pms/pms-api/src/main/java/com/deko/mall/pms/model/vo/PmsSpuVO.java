package com.deko.mall.pms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 商品简要信息VO
 */
@Data
@Accessors(chain = true)
@Schema(description = "商品简要信息")
public class PmsSpuVO {

    @Schema(description = "商品ID")
    private Long id;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "品牌ID")
    private Long brandId;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "销量")
    private Integer sales;

    @Schema(description = "商品主图")
    private String picUrl;

    @Schema(description = "商品图册")
    private String[] album;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "商品详情")
    private String detail;

    @Schema(description = "商品类型")
    private String spuType;

    @Schema(description = "商品类型描述（普通商品）")
    private String spuTypeDesc;

    @Schema(description = "商品状态")
    private Integer status;

    @Schema(description = "会员角标文案 装修动态角标时使用")
    private String subscriptText;
}

