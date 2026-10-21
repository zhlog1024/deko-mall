package com.deko.mall.cms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 商品问答新增/编辑 DTO（管理端；answer/is_reply 由回复接口管理，编辑不触碰）
 */
@Data
@Schema(description = "商品问答新增/编辑DTO")
public class AskSaveDTO {

    @Schema(description = "问答ID(编辑时传)")
    private Long id;

    @Schema(description = "商品SPU ID(空=0不指定商品)")
    private Long spuId;

    @Schema(description = "商品名称快照(提交方携带,后端不查pms)")
    private String spuName;

    @Schema(description = "商品主图快照")
    private String spuPic;

    @NotBlank(message = "问题标题不能为空")
    @Schema(description = "问题标题")
    private String title;

    @Schema(description = "问题详情(可空)")
    private String content;

    @Schema(description = "提问人昵称快照(运营代录可空)")
    private String askUser;

    @Schema(description = "排序(升序),默认0")
    private Integer sort;

    @Schema(description = "是否启用(1是/0否),默认1")
    private Integer isEnable;
}
