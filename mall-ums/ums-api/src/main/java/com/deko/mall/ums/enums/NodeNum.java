package com.deko.mall.ums.enums;

public enum NodeNum {
    ONE(1, "One"),
    TWO(2, "Two"),
    THREE(3, "Three"),
    FOUR(4, "Four");

    private final Integer code;
    private final String description;

    NodeNum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static NodeNum fromCode(Integer code) {
        for (NodeNum level : NodeNum.values()) {
            if (level.getCode() == (code)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown node level code: " + code);
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}