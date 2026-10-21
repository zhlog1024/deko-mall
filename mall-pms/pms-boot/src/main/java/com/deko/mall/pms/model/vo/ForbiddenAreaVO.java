package com.deko.mall.pms.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 禁售区域视图对象
 */
@Data
@Schema(description = "禁售区域视图对象")
public class ForbiddenAreaVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "区域编码集合,多个以逗号分隔")
    private String regionCodes;

    @Schema(description = "区域名称集合,多个以逗号分隔")
    private String regionNames;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
