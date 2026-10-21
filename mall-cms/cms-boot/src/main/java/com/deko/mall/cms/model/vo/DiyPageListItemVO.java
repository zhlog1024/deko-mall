package com.deko.mall.cms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * DIY页面列表项 VO
 */
@Data
@Schema(description = "DIY页面列表项")
public class DiyPageListItemVO {

    @Schema(description = "页面ID")
    private Long id;
    @Schema(description = "页面名称")
    private String name;
    @Schema(description = "页面标题")
    private String title;
    @Schema(description = "封面图")
    private String logo;
}
