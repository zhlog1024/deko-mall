package com.deko.mall.cms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 自定义页面详情 VO（管理端编辑回显,含正文代码）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "自定义页面详情VO")
public class CustomViewDetailVO extends CustomViewVO {

    @Schema(description = "页面HTML代码(整页内容)")
    private String htmlContent;

    @Schema(description = "CSS样式(预留)")
    private String cssContent;

    @Schema(description = "JS代码(预留)")
    private String jsContent;
}
