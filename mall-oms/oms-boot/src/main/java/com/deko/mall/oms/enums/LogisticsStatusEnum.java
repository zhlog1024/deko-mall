package com.deko.mall.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.deko.mall.common.base.IBaseEnum;
import lombok.Getter;

/**
 * 物流状态枚举
 */
@Getter
public enum LogisticsStatusEnum implements IBaseEnum<Integer> {

    PENDING(0, "待揽收"),
    PICKED_UP(1, "已揽收"),
    IN_TRANSIT(2, "运输中"),
    DELIVERING(3, "派送中"),
    SIGNED(4, "已签收"),
    EXCEPTION(5, "异常"),
    RETURNING(6, "退回");

    @EnumValue
    @JsonValue
    private final Integer value;
    private final String label;

    LogisticsStatusEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}
