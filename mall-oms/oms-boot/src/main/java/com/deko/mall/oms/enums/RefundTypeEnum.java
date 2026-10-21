package com.deko.mall.oms.enums;

import com.deko.mall.common.base.IBaseEnum;

/**
 * 退款类型枚举
 */
public enum RefundTypeEnum implements IBaseEnum<Integer> {
    AFTER_SALE_REFUND(2, "售后退款"); // 发货前后退款，统一走售后流程

    private final Integer value;
    private final String label;

    RefundTypeEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    @Override
    public Integer getValue() {
        return this.value;
    }

    @Override
    public String getLabel() {
        return this.label;
    }
}
