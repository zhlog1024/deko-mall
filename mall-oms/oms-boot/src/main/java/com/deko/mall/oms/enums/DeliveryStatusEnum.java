package com.deko.mall.oms.enums;

import com.deko.mall.common.base.IBaseEnum;
import lombok.Getter;

/**
 * 发货状态枚举
 */
@Getter
public enum DeliveryStatusEnum implements IBaseEnum<Integer> {

    DELIVERED(1, "已发货"),
    COMPLETE(2, "已完成");

    private final Integer value;
    private final String label;

    DeliveryStatusEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}
