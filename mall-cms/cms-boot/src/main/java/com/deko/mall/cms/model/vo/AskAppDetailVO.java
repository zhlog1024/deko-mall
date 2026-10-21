package com.deko.mall.cms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品问答 App 详情 VO（含问题详情与回答，浏览数自增）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "商品问答App详情VO")
public class AskAppDetailVO extends AskAppItemVO {

    @Schema(description = "问题详情(可空)")
    private String content;

    @Schema(description = "运营回答内容")
    private String answer;
}
