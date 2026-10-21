package com.deko.mall.system.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * APP版本检测响应
 */
@Schema(description = "APP版本检测结果")
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppVersionCheckDTO {

    @Schema(description = "更新数据")
    @JsonProperty("update_data")
    private UpdateData updateData;

    @Schema(description = "是否显示更新弹窗(1:是 0:否)")
    @JsonProperty("is_update_status")
    private Integer isUpdateStatus;

    @Schema(description = "更新弹窗背景图")
    @JsonProperty("update_alert_bg_images")
    private String updateAlertBgImages;

    @Schema(description = "更新提示间隔时间(秒)")
    @JsonProperty("update_tips_interval_time")
    private Integer updateTipsIntervalTime;

    @Data
    public static class UpdateData {
        @Schema(description = "版本名称")
        private String name;

        @Schema(description = "最新版本号")
        @JsonProperty("version_new")
        private String versionNew;

        @Schema(description = "更新内容")
        private List<String> content;

        @Schema(description = "更新地址")
        @JsonProperty("update_url")
        private String updateUrl;

        @Schema(description = "是否强制更新")
        @JsonProperty("is_force_update")
        private Integer isForceUpdate;
    }
}
