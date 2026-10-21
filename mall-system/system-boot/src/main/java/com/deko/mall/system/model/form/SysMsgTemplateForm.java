package com.deko.mall.system.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 消息模板表单对象
 */
@Data
@Schema(description = "消息模板表单对象")
public class SysMsgTemplateForm {

    @Schema(description = "ID")
    private Long id;

    @NotBlank(message = "模板编码不能为空")
    @Schema(description = "模板编码")
    private String templateCode;

    @NotBlank(message = "模板名称不能为空")
    @Schema(description = "模板名称")
    private String templateName;

    @NotBlank(message = "模板内容不能为空")
    @Schema(description = "模板内容")
    private String templateContent;

    @NotBlank(message = "渠道编码不能为空")
    @Schema(description = "渠道编码")
    private String channelCode;

    @Schema(description = "第三方模板ID")
    private String thirdTemplateId;

    @Schema(description = "模板参数")
    private String templateParams;

    @NotNull(message = "状态不能为空")
    @Schema(description = "状态(0-禁用 1-启用)")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
