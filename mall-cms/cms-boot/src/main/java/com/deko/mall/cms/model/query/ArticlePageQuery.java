package com.deko.mall.cms.model.query;

import com.deko.mall.common.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文章分页查询（管理端）
 */
@Schema(description = "文章分页查询")
@Data
@EqualsAndHashCode(callSuper = true)
public class ArticlePageQuery extends BasePageQuery {

    @Schema(description = "关键字(标题/简介模糊)")
    private String keywords;

    @Schema(description = "分类id(精确)")
    private Long categoryId;

    @Schema(description = "是否启用(1是/0否,空=全部)")
    private Integer isEnable;

}
