package com.deko.mall.oms.enums;

import com.deko.mall.common.base.IBaseEnum;
import lombok.Getter;

/**
 * 售后类型枚举
 */
@Getter
public enum AfterSaleTypeEnum implements IBaseEnum<Integer> {

    REFUND_ONLY(1, "仅退款"),      // 不退货，直接退款
    RETURN_REFUND(2, "退货退款");   // 退货后再退款

    @Getter
    private Integer value;
    @Getter
    private String label;

    AfterSaleTypeEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}
