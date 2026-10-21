package com.deko.mall.cms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 自定义页面 App 渲染 VO（用户端,mp-html 渲染 htmlContent;css/js 小程序端不执行不返回）
 */
@Data
@Schema(description = "自定义页面App渲染VO")
public class CustomViewAppVO {

    @Schema(description = "页面ID")
    private Long id;

    @Schema(description = "logo图片URL")
    private String logo;

    @Schema(description = "页面名称")
    private String name;

    @Schema(description = "页面HTML代码")
    private String htmlContent;

    @Schema(description = "是否包含头部(1是/0否)")
    private Integer isHeader;

    @Schema(description = "是否包含尾部(1是/0否)")
    private Integer isFooter;

    @Schema(description = "是否满屏(1是/0否)")
    private Integer isFullScreen;
}
