package com.deko.mall.cms.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 自定义页面 Feign 数据项（装修器链接/官网选择用）
 */
@Data
@Schema(description = "自定义页面数据项")
public class CustomViewItemDTO implements Serializable {

    @Schema(description = "页面ID")
    private Long id;
    @Schema(description = "页面名称")
    private String name;
    @Schema(description = "logo图片URL")
    private String logo;
}
