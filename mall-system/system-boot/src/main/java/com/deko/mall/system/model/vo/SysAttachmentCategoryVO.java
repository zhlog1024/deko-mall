package com.deko.mall.system.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 附件分类视图对象
 */
@Schema(description = "附件分类视图对象")
@Data
public class SysAttachmentCategoryVO {

    @Schema(description = "分类ID")
    private Long id;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "父级ID（0表示根分类）")
    private Long parentId;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "是否启用（1:启用;0:禁用）")
    private Integer isEnabled;

    @Schema(description = "子分类")
    private List<SysAttachmentCategoryVO> children;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

