package com.deko.mall.common.enums;

import com.deko.mall.common.base.IBaseEnum;
import lombok.Getter;

/**
 * 系统类型枚举
 */
@Getter
public enum SystemTypeEnum implements IBaseEnum<String> {

    PLATFORM("PLATFORM", "平台系统"),
    SUPPLIER("SUPPLIER", "供应商系统");

    @Getter
    private String value;

    @Getter
    private String label;

    SystemTypeEnum(String value, String label) {
        this.value = value;
        this.label = label;
    }
}
