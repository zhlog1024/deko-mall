package com.deko.mall.pms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * App端 - 搜索结果（含筛选选项）
 */
@Data
@Schema(description = "搜索结果视图对象")
public class SearchResultVO {

    @Schema(description = "商品列表")
    private List<SpuPageVO> list;
    @Schema(description = "总条数")
    private Long total;
    @Schema(description = "筛选-品牌列表（仅含搜索结果中出现的品牌）")
    private List<BrandOptionVO> brandList;
    @Schema(description = "筛选-价格区间列表")
    private List<PriceRangeVO> screeningPriceList;
    @Data
    @Schema(description = "品牌选项")
    public static class BrandOptionVO {
        private Long id;
        private String name;
        private String logoUrl;
    }

    @Data
    @Schema(description = "价格区间选项")
    public static class PriceRangeVO {
        private String name;
        private Long minPrice;
        private Long maxPrice;
    }
}
