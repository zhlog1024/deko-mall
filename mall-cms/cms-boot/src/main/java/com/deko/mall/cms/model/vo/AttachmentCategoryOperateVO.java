package com.deko.mall.cms.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 附件分类权限配置 VO
 */
@Data
@Schema(description = "附件分类权限配置")
public class AttachmentCategoryOperateVO {

    @JsonProperty("is_add")
    @Schema(description = "是否允许添加分类")
    private Integer isAdd;

    @JsonProperty("is_edit")
    @Schema(description = "是否允许编辑分类")
    private Integer isEdit;

    @JsonProperty("is_del")
    @Schema(description = "是否允许删除分类")
    private Integer isDel;
}
