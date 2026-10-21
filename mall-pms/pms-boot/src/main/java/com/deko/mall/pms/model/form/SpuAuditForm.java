package com.deko.mall.pms.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品审核表单对象
 */
@Data
public class SpuAuditForm {

    @NotNull(message = "审核状态不能为空")
    @Schema(description = "审核状态(0-待审核,1-审核通过,2-审核拒绝)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer auditStatus;

    @Schema(description = "审核备注")
    private String remark;

    private String auditUser;

    private LocalDateTime auditTime;

    private LocalDateTime submitTime;
}
