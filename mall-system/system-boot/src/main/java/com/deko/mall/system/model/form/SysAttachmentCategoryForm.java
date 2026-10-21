package com.deko.mall.system.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 附件分类表单对象
 */
@Schema(description = "附件分类表单对象")
@Data
public class SysAttachmentCategoryForm {

    @Schema(description = "分类ID（新增时为空）")
    private Long id;

    @Schema(description = "分类名称")
    @NotBlank(message = "分类名称不能为空")
    private String name;

    @Schema(description = "父级ID（0表示根分类）")
    private Long parentId;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "是否启用（1:启用;0:禁用）")
    private Integer isEnabled;
}

