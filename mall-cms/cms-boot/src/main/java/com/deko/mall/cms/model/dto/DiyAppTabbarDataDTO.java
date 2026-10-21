package com.deko.mall.cms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * App 底部导航（tabbar）装修数据读取入参
 */
@Data
@Schema(description = "App底部导航装修数据读取入参")
public class DiyAppTabbarDataDTO {

    @Schema(description = "业务标识（仅支持 home，缺省/空视为 home）")
    private String business;
}
