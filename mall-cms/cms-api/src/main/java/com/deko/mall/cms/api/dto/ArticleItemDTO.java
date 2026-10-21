package com.deko.mall.cms.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文章 Feign 数据项（供装修器/官网等外部消费）
 * 字段语义article 组件数据契约
 */
@Data
@Schema(description = "文章数据项")
public class ArticleItemDTO implements Serializable {

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
    @Schema(description = "标题颜色(十六进制)")
    private String titleColor;
    @Schema(description = "访问量")
    private Integer accessCount;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
