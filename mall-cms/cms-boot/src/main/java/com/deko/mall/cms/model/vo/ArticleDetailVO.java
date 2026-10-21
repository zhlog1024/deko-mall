package com.deko.mall.cms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文章详情 VO（管理端编辑回显）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "文章详情VO")
public class ArticleDetailVO extends ArticleVO {

    @Schema(description = "标题颜色")
    private String titleColor;

    @Schema(description = "外链跳转地址")
    private String jumpUrl;

    @Schema(description = "正文HTML")
    private String content;

    @Schema(description = "图集JSON")
    private String images;

    @Schema(description = "分享图URL")
    private String shareImages;

    @Schema(description = "SEO标题")
    private String seoTitle;

    @Schema(description = "SEO关键字")
    private String seoKeywords;

    @Schema(description = "SEO描述")
    private String seoDesc;
}
