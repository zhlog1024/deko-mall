package com.deko.mall.pms.enums;

import com.deko.mall.common.base.IBaseEnum;
import lombok.Getter;

/**
 * 结算单状态枚举
 */
public enum SettlementStatusEnum implements IBaseEnum<Integer> {

    /**
     * 待确认
     */
    PENDING(0, "待确认"),

    /**
     * 已确认
     */
    CONFIRMED(1, "已确认");

    @Getter
    private final Integer value;

    @Getter
    private final String label;

    SettlementStatusEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}
