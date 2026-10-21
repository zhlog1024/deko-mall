package com.deko.mall.system.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "APP版本检测请求")
@Data
public class AppVersionCheckRequest {

    @Schema(description = "当前APP版本号", example = "v1.0.0 20250101")
    private String appVersion;

    @Schema(description = "平台(android/ios)", example = "android")
    private String platform;
}
