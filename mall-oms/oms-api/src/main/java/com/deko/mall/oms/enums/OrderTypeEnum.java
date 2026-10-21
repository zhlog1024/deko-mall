package com.deko.mall.oms.enums;

import com.deko.mall.common.base.IBaseEnum;
import lombok.Getter;

/**
 * 订单状态枚举
 */
@Getter
public enum OrderTypeEnum implements IBaseEnum<String> {

    /**
     * 普通订单
     */
    NORMAL("NORMAL", "普通订单");

    @Getter
    private String value;
    @Getter
    private String label;

    OrderTypeEnum(String value, String label) {
        this.value = value;
        this.label = label;
    }

}
