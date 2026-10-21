package com.deko.mall.cms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DiyPageHomeDTO {

    @Schema(description = "页面ID")
    @NotNull(message = "页面ID不能为空")
    private Long id;
}

