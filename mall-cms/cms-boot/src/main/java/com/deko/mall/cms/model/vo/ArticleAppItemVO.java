package com.deko.mall.cms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章 App 列表项 VO（用户端渲染，字段语义article 组件）
 */
@Data
@Schema(description = "文章App列表项VO")
public class ArticleAppItemVO {

    @Schema(description = "文章ID")
    private Long id;
    @Schema(description = "标题")
    private String title;
    @Schema(description = "简介/描述")
    private String description;
    @Schema(description = "封面URL")
    private String cover;
    @Schema(description = "外链跳转地址")
    private String jumpUrl;
    @Schema(description = "标题颜色(十六进制,app列表标题着色)")
    private String titleColor;
    @Schema(description = "访问量")
    private Integer accessCount;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
