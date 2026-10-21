package com.deko.mall.cms.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 附件权限配置 VO
 */
@Data
@Schema(description = "附件权限配置")
public class AttachmentOperateVO {

    @JsonProperty("is_upload")
    @Schema(description = "是否允许上传")
    private Integer isUpload;

    @JsonProperty("is_del")
    @Schema(description = "是否允许删除")
    private Integer isDel;

    @JsonProperty("is_move")
    @Schema(description = "是否允许移动")
    private Integer isMove;

    @JsonProperty("is_edit")
    @Schema(description = "是否允许编辑")
    private Integer isEdit;
}
