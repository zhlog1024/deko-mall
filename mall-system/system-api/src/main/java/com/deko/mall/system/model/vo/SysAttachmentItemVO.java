package com.deko.mall.system.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 附件项视图对象
 */
@Data
@Schema(description = "附件项视图对象")
public class SysAttachmentItemVO {

    @Schema(description = "附件ID")
    private Long id;

    @Schema(description = "附件名称")
    private String name;

    @Schema(description = "原始文件名")
    private String originalName;

    @Schema(description = "文件路径")
    private String filePath;

    @Schema(description = "文件URL")
    private String fileUrl;

    @Schema(description = "文件类型")
    private String fileType;

    @Schema(description = "文件大小")
    private Long fileSize;

    @Schema(description = "文件扩展名")
    private String fileExt;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;
}
