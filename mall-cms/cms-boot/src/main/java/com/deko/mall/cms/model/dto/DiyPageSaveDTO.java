package com.deko.mall.cms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DiyPageSaveDTO {

    @Schema(description = "页面ID，编辑时必填")
    private Long id;

    @Schema(description = "页面名称")
    @NotBlank(message = "页面名称不能为空")
    private String name;

    @Schema(description = "装修JSON配置")
    private String config;

    @Schema(description = "封面图")
    private String coverUrl;

    @Schema(description = "是否启用")
    private Boolean isEnabled;
}

