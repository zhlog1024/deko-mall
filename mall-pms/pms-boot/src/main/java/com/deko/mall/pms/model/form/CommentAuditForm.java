package com.deko.mall.pms.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 评论审核表单
 */
@Data
public class CommentAuditForm {

    @Schema(description = "状态 1-通过 2-拒绝")
    @NotNull(message = "审核状态不能为空")
    private Integer status;
}
