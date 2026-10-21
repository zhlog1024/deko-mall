package com.deko.mall.cms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文章 App 详情 VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "文章App详情VO")
public class ArticleAppDetailVO extends ArticleAppItemVO {

    @Schema(description = "正文HTML")
    private String content;
}
