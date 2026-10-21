package com.deko.mall.ums.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 智能地址识别请求
 */
@Data
@Schema(description = "智能地址识别请求")
public class AddressParseReq {

    @Schema(description = "原始地址文本，如：张三 13800138000 北京市朝阳区望京SOHO")
    private String rawAddress;
}
