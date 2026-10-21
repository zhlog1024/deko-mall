package com.deko.mall.cms.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * DIY 装修器数据列表 VO（用于分页列表）
 * data_list / data_total / page
 * 使用泛型支持不同类型的列表数据（装修页面、商品、文章等）
 */
@Data
@Schema(description = "DIY装修器数据列表")
public class DiyApiDataVO {

    @Schema(description = "数据列表")
    @JsonProperty("data_list")
    private List<?> dataList;
    @Schema(description = "总记录数")
    @JsonProperty("data_total")
    private Long dataTotal;
    @Schema(description = "总页数")
    @JsonProperty("page_total")
    private Integer pageTotal;
    @Schema(description = "当前页码")
    @JsonProperty("page")
    private Integer page;
    @Schema(description = "每页数量")
    @JsonProperty("page_size")
    private Integer size;
}

