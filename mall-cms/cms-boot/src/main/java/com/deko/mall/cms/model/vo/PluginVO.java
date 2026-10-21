package com.deko.mall.cms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 插件项 VO
 */
@Data
@Schema(description = "插件项")
public class PluginVO {

    @Schema(description = "插件标识")
    private String key;

    @Schema(description = "插件名称")
    private String name;
}
