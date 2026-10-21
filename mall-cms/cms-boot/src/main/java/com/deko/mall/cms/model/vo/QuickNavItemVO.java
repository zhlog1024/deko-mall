package com.deko.mall.cms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 快捷入口项 VO
 */
@Data
@Schema(description = "快捷入口项")
public class QuickNavItemVO {

    @Schema(description = "名称")
    private String name;

    @Schema(description = "类型")
    private String type;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "索引")
    private Integer index;

    @Schema(description = "是否显示")
    private Integer isShow;

    @Schema(description = "值")
    private String value;

    @Schema(description = "链接地址")
    private String url;
}
