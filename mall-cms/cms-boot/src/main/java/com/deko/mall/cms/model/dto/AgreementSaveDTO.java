package com.deko.mall.cms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 协议新增与编辑参数。
 */
@Data
@Schema(description = "协议新增/编辑DTO")
public class AgreementSaveDTO {

    @Schema(description = "协议ID(编辑时传)")
    private Long id;

    @NotBlank(message = "协议标识不能为空")
    @Schema(description = "协议标识(userregister/userprivacy/...)")
    private String document;

    @NotBlank(message = "协议标题不能为空")
    @Schema(description = "协议标题")
    private String title;

    @Schema(description = "协议内容(HTML富文本)")
    private String content;

    @Schema(description = "状态(1启用/0禁用),默认1")
    private Integer status;
}
