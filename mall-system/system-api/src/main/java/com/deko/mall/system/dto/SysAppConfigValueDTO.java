package com.deko.mall.system.dto;

import lombok.Data;

/**
 * APP 配置值写入 DTO
 */
@Data
public class SysAppConfigValueDTO {

    /** 配置值（字符串形态，JSON 配置由调用方序列化后传入） */
    private String value;
}
