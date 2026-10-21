package com.deko.mall.cms.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 文章自动数据查询（Feign，装修器"自动数据"模式 / 用户端组件渲染）
 * 参数语义diyapi/articleautodata：分类(含子级)、封面过滤、排序、数量
 */
@Data
@Schema(description = "文章自动数据查询")
public class ArticleAutoQuery implements Serializable {

    @Schema(description = "分类ID列表(含子分类递归)")
    private List<Long> categoryIds;
    @Schema(description = "是否仅取有封面文章(1=是)")
    private Integer onlyCover;
    @Schema(description = "关键字(标题/简介模糊)")
    private String keyword;
    @Schema(description = "排序类型:0综合(访问+id) 1人气(访问) 2最新(id)")
    private Integer orderByType;
    @Schema(description = "排序规则:asc/desc")
    private String orderByRule;
    @Schema(description = "数量(默认10)")
    private Integer number;
}
