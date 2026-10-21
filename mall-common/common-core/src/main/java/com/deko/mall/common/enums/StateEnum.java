package com.deko.mall.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum StateEnum {

    VALID(1, "有效"),
    INVALID(0, "无效");

    @JsonValue
    @EnumValue
    private int code;
    private String desc;

    StateEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @JsonCreator
    public static StateEnum toEnum(int code) {
        for (StateEnum tmp : values()) {
            if (tmp.getCode() == code) {
                return tmp;
            }
        }
        throw new IllegalStateException("StateEnum-枚举" + code + "不存在");
    }

}
