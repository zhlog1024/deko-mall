package com.deko.mall.pms.enums;

import com.deko.mall.common.base.IBaseEnum;
import lombok.Getter;

/**
 * 结算周期枚举
 */
public enum SettlementCycleEnum implements IBaseEnum<Integer> {

    /**
     * 按月结算
     */
    MONTHLY(1, "按月结算"),

    /**
     * 按季度结算
     */
    QUARTERLY(2, "按季度结算");

    @Getter
    private final Integer value;

    @Getter
    private final String label;

    SettlementCycleEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    /**
     * 根据值获取名称
     *
     * @param value 枚举值
     * @return 枚举名称
     */
    public static String getNameByValue(Integer value) {
        if (value == null) {
            return "";
        }
        for (SettlementCycleEnum cycleEnum : values()) {
            if (cycleEnum.getValue().equals(value)) {
                return cycleEnum.getLabel();
            }
        }
        return "";
    }
}
