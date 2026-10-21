package com.deko.mall.system.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 附件表单对象
 */
@Schema(description = "附件表单对象")
@Data
public class SysAttachmentForm {

    @Schema(description = "附件ID（更新时必填）")
    private Long id;

    @Schema(description = "附件名称")
    @NotBlank(message = "附件名称不能为空")
    private String name;

    @Schema(description = "分类ID")
    private Long categoryId;
}

