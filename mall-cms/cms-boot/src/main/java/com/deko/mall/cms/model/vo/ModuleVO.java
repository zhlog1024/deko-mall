package com.deko.mall.cms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 组件模块 VO
 */
@Data
@Schema(description = "组件模块")
public class ModuleVO {

    @Schema(description = "组件标识")
    private String key;

    @Schema(description = "组件名称")
    private String name;
}
