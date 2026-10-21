package com.deko.mall.system.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 附件查询对象
 */
@Data
@Schema(description = "附件查询对象")
public class SysAttachmentQuery {

    @Schema(description = "页码")
    private Integer pageNum = 1;

    @Schema(description = "每页数量")
    private Integer pageSize = 10;

    @Schema(description = "关键词")
    private String keywords;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "文件类型")
    private String fileType;
}
