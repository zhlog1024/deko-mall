package com.deko.mall.pay.api.enums;

import com.deko.mall.common.base.IBaseEnum;
import lombok.Getter;

/**
 * 退款状态枚举
 */
@Getter
public enum RefundStatusEnum implements IBaseEnum<Integer> {

    /** 退款中 */
    REFUNDING(1, "退款中"),

    /** 退款成功 */
    REFUND_SUCCESS(2, "退款成功"),

    /** 退款失败 */
    REFUND_FAIL(3, "退款失败");

    private final Integer value;
    private final String label;

    RefundStatusEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}
