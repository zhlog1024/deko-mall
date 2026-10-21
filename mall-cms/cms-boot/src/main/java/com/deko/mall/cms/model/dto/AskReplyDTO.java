package com.deko.mall.cms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 商品问答运营回复 DTO（回复即确认展示：置 answer、is_reply=1，同时自动 is_enable=1）
 */
@Data
@Schema(description = "商品问答回复DTO")
public class AskReplyDTO {

    @NotBlank(message = "回答内容不能为空")
    @Schema(description = "回答内容")
    private String answer;

    @Schema(description = "回答人昵称快照(空则取当前登录人)")
    private String replyUser;
}
