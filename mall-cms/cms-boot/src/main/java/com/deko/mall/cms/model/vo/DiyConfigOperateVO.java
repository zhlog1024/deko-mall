package com.deko.mall.cms.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * DIY装修器操作权限配置 VO
 */
@Data
@Schema(description = "装修器操作权限配置")
public class DiyConfigOperateVO {

    @JsonProperty("is_base_data")
    @Schema(description = "是否允许基础信息编辑")
    private Integer isBaseData;

    @JsonProperty("is_upload_admin")
    @Schema(description = "是否显示上传管理按钮")
    private Integer isUploadAdmin;

    @JsonProperty("is_save_button")
    @Schema(description = "是否显示保存按钮")
    private Integer isSaveButton;

    @JsonProperty("is_save_close_button")
    @Schema(description = "是否显示保存关闭按钮")
    private Integer isSaveCloseButton;
}
