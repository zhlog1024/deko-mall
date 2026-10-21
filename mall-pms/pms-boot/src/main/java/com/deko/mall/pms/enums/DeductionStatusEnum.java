package com.deko.mall.pms.enums;

import com.deko.mall.common.base.IBaseEnum;
import lombok.Getter;

/**
 * 扣款状态枚举
 */
@Getter
public enum DeductionStatusEnum implements IBaseEnum<Integer> {

    PENDING(0, "未扣款"),
    DEDUCTED(1, "已扣款");

    private final Integer value;
    private final String label;

    DeductionStatusEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}
