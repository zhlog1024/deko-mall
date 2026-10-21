package com.deko.mall.cms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品问答详情 VO（管理端编辑回显，含问题详情与回答）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "商品问答详情VO")
public class AskDetailVO extends AskVO {

    @Schema(description = "问题详情(可空)")
    private String content;

    @Schema(description = "运营回答内容")
    private String answer;
}
