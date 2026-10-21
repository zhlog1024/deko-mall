package com.deko.mall.oms.enums;

import com.deko.mall.common.base.IBaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 物流类型枚举
 */
@Getter
@AllArgsConstructor
public enum LogisticsTypeEnum implements IBaseEnum<Integer> {

    /**
     * 发货物流
     */
    DELIVERY(1, "发货物流"),

    /**
     * 退货物流
     */
    RETURN(2, "退货物流");

    private final Integer value;
    private final String label;
}
