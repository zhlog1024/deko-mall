package com.deko.mall.oms.enums;

import com.deko.mall.common.base.IBaseEnum;
import lombok.Getter;

/**
 * 购买渠道枚举
 */
@Getter
public enum PurchaseChannelEnum implements IBaseEnum<String> {

    /**
     * 普通购买渠道
     */
    NORMAL("NORMAL", "普通购买");

    private final String value;
    private final String label;

    PurchaseChannelEnum(String value, String label) {
        this.value = value;
        this.label = label;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
