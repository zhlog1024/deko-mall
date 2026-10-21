package com.deko.mall.cms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 视频启用状态 DTO（运营启用/停用）
 */
@Data
@Schema(description = "视频启用状态DTO")
public class VideoEnableDTO {

    @NotNull(message = "启用状态不能为空")
    @Schema(description = "是否启用(1是/0否)")
    private Integer isEnable;
}
