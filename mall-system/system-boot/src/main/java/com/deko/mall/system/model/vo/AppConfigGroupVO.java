package com.deko.mall.system.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "配置分组VO")
public class AppConfigGroupVO {

    @Schema(description = "分组代码")
    private String groupCode;

    @Schema(description = "分组名称")
    private String groupName;

    @Schema(description = "配置项列表")
    private List<AppConfigItemVO> items;
}
