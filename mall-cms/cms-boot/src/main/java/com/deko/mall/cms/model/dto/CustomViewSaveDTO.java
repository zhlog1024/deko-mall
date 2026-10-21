package com.deko.mall.cms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 自定义页面新增/编辑 DTO
 */
@Data
@Schema(description = "自定义页面新增/编辑DTO")
public class CustomViewSaveDTO {

    @Schema(description = "页面ID(编辑时传)")
    private Long id;

    @Schema(description = "logo图片URL")
    private String logo;

    @NotBlank(message = "页面名称不能为空")
    @Schema(description = "页面名称")
    private String name;

    @Schema(description = "页面HTML代码(整页内容)")
    private String htmlContent;

    @Schema(description = "CSS样式(预留)")
    private String cssContent;

    @Schema(description = "JS代码(预留)")
    private String jsContent;

    @Schema(description = "是否启用(1是/0否),默认1")
    private Integer isEnable;

    @Schema(description = "是否包含头部(1是/0否),默认1")
    private Integer isHeader;

    @Schema(description = "是否包含尾部(1是/0否),默认1")
    private Integer isFooter;

    @Schema(description = "是否满屏(1是/0否),默认0")
    private Integer isFullScreen;

    @Schema(description = "SEO标题")
    private String seoTitle;

    @Schema(description = "SEO关键字")
    private String seoKeywords;

    @Schema(description = "SEO描述")
    private String seoDesc;
}
