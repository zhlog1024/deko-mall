package com.deko.mall.system.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 附件信息视图对象
 */
@Schema(description = "附件信息视图对象")
@Data
public class SysAttachmentVO {

    @Schema(description = "附件ID")
    private Long id;

    @Schema(description = "附件名称")
    private String name;

    @Schema(description = "原始文件名")
    private String originalName;

    @Schema(description = "文件路径（OSS路径）")
    private String filePath;

    @Schema(description = "文件访问URL")
    private String fileUrl;

    @Schema(description = "文件类型（image/video/file）")
    private String fileType;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "文件扩展名")
    private String fileExt;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

