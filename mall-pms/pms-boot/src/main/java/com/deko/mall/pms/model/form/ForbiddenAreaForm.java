package com.deko.mall.pms.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


/**
 * 禁售区域表单对象
 */
@Data
@Schema(description = "禁售区域表单对象")
public class ForbiddenAreaForm {

    @Schema(description = "供应商ID")
    private Long supplierId;

    @NotBlank(message = "区域编码不能为空")
    @Schema(description = "区域编码集合,多个以逗号分隔", required = true)
    private String regionCodes;

    @NotBlank(message = "区域名称不能为空")
    @Schema(description = "区域名称集合,多个以逗号分隔", required = true)
    private String regionNames;

    @Schema(description = "备注")
    private String remark;
}
