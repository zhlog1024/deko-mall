package com.deko.mall.cms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 自定义页面 VO（管理端列表,不含正文代码,避免大字段）
 */
@Data
@Schema(description = "自定义页面VO")
public class CustomViewVO {

    @Schema(description = "页面ID")
    private Long id;

    @Schema(description = "logo图片URL")
    private String logo;

    @Schema(description = "页面名称")
    private String name;

    @Schema(description = "是否启用(1是/0否)")
    private Integer isEnable;

    @Schema(description = "是否包含头部(1是/0否)")
    private Integer isHeader;

    @Schema(description = "是否包含尾部(1是/0否)")
    private Integer isFooter;

    @Schema(description = "是否满屏(1是/0否)")
    private Integer isFullScreen;

    @Schema(description = "访问量")
    private Integer accessCount;

    @Schema(description = "SEO标题")
    private String seoTitle;

    @Schema(description = "SEO关键字")
    private String seoKeywords;

    @Schema(description = "SEO描述")
    private String seoDesc;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
