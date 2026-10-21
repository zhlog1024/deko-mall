package com.deko.mall.cms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 文章新增/编辑 DTO
 */
@Data
@Schema(description = "文章新增/编辑DTO")
public class ArticleSaveDTO {

    @Schema(description = "文章ID(编辑时传)")
    private Long id;

    @NotBlank(message = "标题不能为空")
    @Schema(description = "标题")
    private String title;

    @Schema(description = "简介/描述")
    private String description;

    @NotNull(message = "分类不能为空")
    @Schema(description = "分类id")
    private Long categoryId;

    @Schema(description = "标题颜色(十六进制)")
    private String titleColor;

    @Schema(description = "外链跳转地址")
    private String jumpUrl;

    @Schema(description = "是否启用(1是/0否),默认1")
    private Integer isEnable;

    @Schema(description = "正文HTML")
    private String content;

    @Schema(description = "封面URL")
    private String cover;

    @Schema(description = "图集JSON(一维url数组)")
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
