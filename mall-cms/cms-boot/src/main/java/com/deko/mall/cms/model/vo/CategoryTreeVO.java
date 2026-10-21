package com.deko.mall.cms.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 分类树项 VO（用于商品分类、附件分类等）
 */
@Data
@Schema(description = "分类树项")
public class CategoryTreeVO {

    @Schema(description = "分类ID")
    private Long id;
    @Schema(description = "分类名称")
    private String name;
    @Schema(description = "父分类ID")
    private Long parentId;
    @Schema(description = "层级")
    private Integer level;
    @Schema(description = "子分类")
    @JsonProperty("items")  // 序列化输出 items（cascader children）
    private List<CategoryTreeVO> children;
}
