package com.deko.mall.pms.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 供应商审核表单对象
 */
@Data
@Schema(description = "供应商审核表单对象")
public class SupplierAuditForm {

    @NotNull(message = "审核状态不能为空")
    @Schema(description = "审核状态(1-审核通过,2-审核拒绝)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer auditStatus;

    @Schema(description = "审核备注")
    private String remark;
}
