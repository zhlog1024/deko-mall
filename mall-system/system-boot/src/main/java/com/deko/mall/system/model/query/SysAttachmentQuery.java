package com.deko.mall.system.model.query;

import com.deko.mall.common.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 附件查询对象
 */
@Schema(description = "附件查询对象")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysAttachmentQuery extends BasePageQuery {

    @Schema(description = "关键字（附件名称/原始文件名）")
    private String keywords;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "文件类型（image/video/file）")
    private String fileType;
}

