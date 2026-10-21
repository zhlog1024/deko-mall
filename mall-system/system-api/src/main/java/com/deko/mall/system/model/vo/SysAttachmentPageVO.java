package com.deko.mall.system.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 附件分页视图对象
 */
@Data
@Schema(description = "附件分页视图对象")
public class SysAttachmentPageVO {

    @Schema(description = "附件列表")
    private List<SysAttachmentItemVO> records;

    @Schema(description = "总数")
    private Long total;

    @Schema(description = "每页数量")
    private Long size;

    @Schema(description = "当前页")
    private Long current;

    @Schema(description = "总页数")
    private Long pages;
}
