package com.deko.mall.pms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 【应用端】商品分页视图对象
 */
@Schema(description = "商品分页对象")
@Data
public class SpuPageVO {

    @Schema(description = "商品ID")
    private Long id;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "商品价格(单位：元)")
    private BigDecimal price;

    @Schema(description = "商品原价(单位：元)")
    private BigDecimal originPrice;
    @Schema(description = "销量")
    private Integer sales;

    @Schema(description = "图片地址")
    private String picUrl;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "品牌名称")
    private String brandName;

    @Schema(description = "商品简述/简介")
    private String simpleDesc;

    /**
     * 商品类型
     */
    private String spuType;

    /**
     * 商品类型描述
     */
    private String spuTypeDesc;

    @Schema(description = "会员角标文案 ，装修动态角标时使用")
    private String subscriptText;

    @Schema(description = "默认SKU ID（取该SPU下第一个SKU）")
    private Long defaultSkuId;

    @Schema(description = "是否多规格(0=单规格, 1=多规格)")
    private Integer isExistManySpec;
}
