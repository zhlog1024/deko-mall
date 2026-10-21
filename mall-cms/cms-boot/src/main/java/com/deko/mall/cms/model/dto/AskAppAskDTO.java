package com.deko.mall.cms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 商品问答 App 用户提问 DTO（提交即入库 is_enable=0，运营确认/回复后展示）
 */
@Data
@Schema(description = "商品问答App提问DTO")
public class AskAppAskDTO {

    @NotNull(message = "商品不能为空")
    @Schema(description = "商品SPU ID")
    private Long spuId;

    @Schema(description = "商品名称快照(提交方携带)")
    private String spuName;

    @Schema(description = "商品主图快照")
    private String spuPic;

    @NotBlank(message = "问题标题不能为空")
    @Schema(description = "问题标题")
    private String title;

    @Schema(description = "问题详情(可空)")
    private String content;
}
