package com.deko.mall.pay.api.enums;

import com.deko.mall.common.base.IBaseEnum;
import lombok.Getter;

/**
 * 支付状态枚举
 */
@Getter
public enum PayStatusEnum implements IBaseEnum<Integer> {

    WAIT_PAY(0, "待支付"),
    PAYING(1, "支付中"),
    PAY_SUCCESS(2, "支付成功"),
    PAY_FAIL(3, "支付失败"),
    CLOSED(4, "已关闭");

    @Getter
    private Integer value;
    @Getter
    private String label;

    PayStatusEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}
