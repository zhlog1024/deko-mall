package com.deko.mall.cms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * App 底部导航（tabbar）装修保存入参
 * diyapi/apptabbarsave 的 {config, business} 结构
 */
@Data
@Schema(description = "App底部导航装修保存入参")
public class DiyAppTabbarSaveDTO {

    @Schema(description = "业务标识（仅支持 home，缺省/空视为 home）")
    private String business;
    @Schema(description = "底部导航装修配置（content/style 结构，原样 JSON 序列化存储）")
    @NotNull(message = "底部导航配置不能为空")
    private Object config;
}
