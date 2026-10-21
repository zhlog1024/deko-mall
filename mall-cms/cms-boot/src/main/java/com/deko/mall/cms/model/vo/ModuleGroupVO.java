package com.deko.mall.cms.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 组件模块分组 VO
 */
@Data
@Schema(description = "组件模块分组")
public class ModuleGroupVO {

    @Schema(description = "分组名称")
    private String name;
    @Schema(description = "分组标识")
    private String key;
    @Schema(description = "组件列表")
    @JsonProperty("data")
    private List<ModuleVO> data;
}
