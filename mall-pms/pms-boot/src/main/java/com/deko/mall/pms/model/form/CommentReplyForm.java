package com.deko.mall.pms.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 评论回复表单
 */
@Data
public class CommentReplyForm {

    @Schema(description = "回复内容")
    @NotBlank(message = "回复内容不能为空")
    private String replyContent;
}
