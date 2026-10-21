package com.deko.mall.cms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 底部导航项 VO
 */
@Data
@Schema(description = "底部导航项")
public class TabbarItemVO {

    @Schema(description = "页面路径")
    private String url;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "选中图标")
    private String activeIcon;

    @Schema(description = "索引")
    private Integer index;

    @Schema(description = "是否显示")
    private Integer isShow;

    @Schema(description = "是否自定义")
    private Integer isDiy;
}
