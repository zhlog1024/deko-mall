package com.deko.mall.cms.model.query;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * DIY 装修器 - 商品查询参数
 */
@Data
@Schema(description = "商品查询参数")
public class DiyGoodsQuery {

    @Schema(description = "当前页码")
    private Integer page;

    @Schema(description = "每页数量")
    @JsonProperty("page_size")
    private Integer pageSize;

    @Schema(description = "搜索关键词")
    private String keywords;

    @Schema(description = "品牌ID")
    @JsonProperty("brand_ids")
    private String brandIds;

    @Schema(description = "分类ID（单选）")
    @JsonProperty("category_id")
    private Long categoryId;

    @Schema(description = "分类级别(1-一级,2-二级,3-三级)")
    @JsonProperty("category_level")
    private Integer categoryLevel;

    @Schema(description = "商品类型")
    @JsonProperty("spu_type")
    private String spuType;
}
