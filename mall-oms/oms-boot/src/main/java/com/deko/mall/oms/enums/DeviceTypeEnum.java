package com.deko.mall.oms.enums;

import lombok.Getter;

/**
 * 设备类型枚举
 */
@Getter
public enum DeviceTypeEnum {

    H5("H5", "H5网页"),
    APP("APP", "APP客户端"),
    MINI("MINI", "小程序"),
    PC("PC", "PC网页");

    private final String code;
    private final String desc;

    DeviceTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static DeviceTypeEnum getByCode(String code) {
        for (DeviceTypeEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
