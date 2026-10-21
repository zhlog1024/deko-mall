package com.deko.mall.pms.enums;

import lombok.Getter;

/**
 * 商品属性类型枚举
 */
public enum AttributeTypeEnum {

    SPEC(1, "规格"),
    ATTR(2, "属性");

    @Getter
    private Integer value;
    @Getter
    private String name;

    AttributeTypeEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static AttributeTypeEnum getByValue(Integer value) {
        AttributeTypeEnum attributeTypeEnum = null;

        for (AttributeTypeEnum item : values()) {
            if (item.getValue().equals(value)) {
                attributeTypeEnum = item;
            }
        }
        return attributeTypeEnum;
    }

}
