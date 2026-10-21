package com.deko.mall.pms.enums;

import com.deko.mall.common.base.IBaseEnum;
import lombok.Getter;

/**
 * 是否通用枚举
 */
@Getter
public enum YesNoEnum implements IBaseEnum<Integer> {

    /**
     * 否
     */
    NO(0, "否"),

    /**
     * 是
     */
    YES(1, "是");

    private final Integer value;
    private final String label;

    YesNoEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}
