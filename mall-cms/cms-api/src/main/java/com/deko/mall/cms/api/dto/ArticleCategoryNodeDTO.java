package com.deko.mall.cms.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 文章分类树节点（Feign 契约，供装修器分类选择器等消费）
 */
@Data
@Schema(description = "文章分类树节点")
public class ArticleCategoryNodeDTO implements Serializable {

    @Schema(description = "分类ID")
    private Long id;
    @Schema(description = "父分类ID(0=顶级)")
    private Long pid;
    @Schema(description = "分类名称")
    private String name;
    @Schema(description = "子分类")
    private List<ArticleCategoryNodeDTO> children;
}
