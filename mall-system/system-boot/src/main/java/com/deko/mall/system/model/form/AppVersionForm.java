package com.deko.mall.system.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "APP版本表单")
@Data
public class AppVersionForm {

    @Schema(description = "ID(编辑时必填)")
    private Long id;

    @NotBlank(message = "版本名称不能为空")
    @Schema(description = "版本名称")
    private String versionName;

    @NotBlank(message = "版本号不能为空")
    @Schema(description = "版本号")
    private String versionCode;

    @NotBlank(message = "平台不能为空")
    @Schema(description = "平台(android/ios)")
    private String platform;

    @Schema(description = "更新下载地址")
    private String updateUrl;

    @Schema(description = "更新内容(JSON数组)")
    private String updateContent;

    @Schema(description = "是否强制更新(0:否 1:是)")
    private Integer isForceUpdate;

    @NotNull(message = "启用状态不能为空")
    @Schema(description = "是否启用(0:否 1:是)")
    private Integer isEnabled;
}
