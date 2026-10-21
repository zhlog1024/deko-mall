package com.deko.mall.cms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DiyPagePublishDTO {

    @Schema(description = "页面ID")
    @NotNull(message = "页面ID不能为空")
    private Long id;

    @Schema(description = "生效开始时间")
    private LocalDateTime timeStart;

    @Schema(description = "生效结束时间")
    private LocalDateTime timeEnd;

    @Schema(description = "是否启用，默认true")
    private Boolean isEnabled;
}

