package com.deko.mall.cms.model.query;

import com.deko.mall.common.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 文章 App 端列表查询（用户端渲染，语义articleautodata）
 */
@Schema(description = "文章App列表查询")
@Data
@EqualsAndHashCode(callSuper = true)
public class ArticleAppQuery extends BasePageQuery {

    @Schema(description = "分类id(含子分类递归,与categoryIds二选一)")
    private Long categoryId;
    @Schema(description = "分类id集合(含子分类递归,逗号分隔,与categoryId可叠加取并集)")
    private List<Long> categoryIds;
    @Schema(description = "是否仅取有封面文章(1=是)")
    private Integer onlyCover;
    @Schema(description = "关键字(标题/简介模糊)")
    private String keyword;
    @Schema(description = "排序类型:0综合(访问+id) 1人气(访问) 2最新(id),默认0")
    private Integer orderByType;
    @Schema(description = "排序规则:asc/desc,默认desc")
    private String orderByRule;
}
