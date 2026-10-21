package com.deko.mall.pms.enums;

import com.deko.mall.common.base.IBaseEnum;
import lombok.Getter;

/**
 * 计费方式枚举
 */
@Getter
public enum ChargeTypeEnum implements IBaseEnum<String> {

    /**
     * 按件数
     */
    BY_PIECE("BY_PIECE", "按件数"),

    /**
     * 按重量
     */
    BY_WEIGHT("BY_WEIGHT", "按重量");

    private final String value;
    private final String label;

    ChargeTypeEnum(String value, String label) {
        this.value = value;
        this.label = label;
    }
}
