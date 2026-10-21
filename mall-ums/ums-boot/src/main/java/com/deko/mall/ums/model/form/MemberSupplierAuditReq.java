package com.deko.mall.ums.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 供应商信息审核
 */
@Schema(description = "供应商审核-请求入参")
@Data
public class MemberSupplierAuditReq implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "审核状态不能为空！")
    @Schema(description = "审核状态：10待审核，20，通过。30拒绝")
    private Integer auditStatus;

    @Schema(description = "原因描述")
    private String desc;

    @NotNull(message = "供应商id不能为空！")
    @Schema(description = "供应商id")
    private Long supplierId;

    private Long auditUser;
}