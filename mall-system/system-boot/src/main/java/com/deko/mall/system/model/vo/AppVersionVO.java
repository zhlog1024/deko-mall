package com.deko.mall.system.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "APP版本视图对象")
@Data
public class AppVersionVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "版本名称")
    private String versionName;

    @Schema(description = "版本号")
    private String versionCode;

    @Schema(description = "平台")
    private String platform;

    @Schema(description = "更新下载地址")
    private String updateUrl;

    @Schema(description = "更新内容(JSON数组)")
    private String updateContent;

    @Schema(description = "是否强制更新(0:否 1:是)")
    private Integer isForceUpdate;

    @Schema(description = "是否启用(0:否 1:是)")
    private Integer isEnabled;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
