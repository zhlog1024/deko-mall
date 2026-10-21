package com.deko.mall.system.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 消息模板分页查询VO
 */
@Data
@Schema(description = "消息模板分页查询对象")
public class SysMsgTemplatePageVO {

    @Schema(description = "模板编码")
    private String templateCode;

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "渠道编码")
    private String channelCode;

    @Schema(description = "状态(0-禁用 1-启用)")
    private Integer status;
}
