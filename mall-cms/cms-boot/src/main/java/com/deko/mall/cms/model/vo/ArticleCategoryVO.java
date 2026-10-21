package com.deko.mall.cms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 文章分类树节点 VO
 */
@Data
@Schema(description = "文章分类树节点VO")
public class ArticleCategoryVO {

    @Schema(description = "分类ID")
    private Long id;

    @Schema(description = "父分类id(0=顶级)")
    private Long pid;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "是否启用(1是/0否)")
    private Integer isEnable;

    @Schema(description = "子分类")
    private List<ArticleCategoryVO> children = new ArrayList<>();
}
