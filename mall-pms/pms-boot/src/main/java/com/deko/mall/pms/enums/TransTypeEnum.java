package com.deko.mall.pms.enums;

import com.deko.mall.common.base.IBaseEnum;
import lombok.Getter;

/**
 * 交易类型枚举
 */
@Getter
public enum TransTypeEnum implements IBaseEnum<String> {

    // 收入类
    SETTLEMENT("SETTLEMENT", "订单结算"),
    REFUND("REFUND", "订单退款"),
    WITHDRAWAL_REJECTED("WITHDRAWAL_REJECTED", "提现拒绝"),
    WITHDRAWAL_FAILED("WITHDRAWAL_FAILED", "提现失败"),

    // 支出类
    WITHDRAWAL_APPLY("WITHDRAWAL_APPLY", "提现申请"),
    DEDUCTION("DEDUCTION", "系统扣款");

    private final String value;
    private final String label;

    TransTypeEnum(String value, String label) {
        this.value = value;
        this.label = label;
    }
}
