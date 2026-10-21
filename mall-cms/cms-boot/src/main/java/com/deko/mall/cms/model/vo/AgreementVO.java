package com.deko.mall.cms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 协议 VO（admin/app 共用，T1）
 */
@Data
@Schema(description = "协议VO")
public class AgreementVO {

    @Schema(description = "协议ID")
    private Long id;

    @Schema(description = "协议标识")
    private String document;

    @Schema(description = "协议标题")
    private String title;

    @Schema(description = "协议内容(HTML富文本)")
    private String content;

    @Schema(description = "状态(1启用/0禁用)")
    private Integer status;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "生效时间")
    private LocalDateTime effectiveTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
