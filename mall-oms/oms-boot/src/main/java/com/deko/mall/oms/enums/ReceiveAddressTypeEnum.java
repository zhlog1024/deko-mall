package com.deko.mall.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ReceiveAddressTypeEnum {

    MERCHANT_RECEIVE("MERCHANT_RECEIVE", "商家收货"),// 退货地址
    USER_RECEIVE("USER_RECEIVE", "用户收货"),
    ;
    @JsonValue
    @EnumValue
    private String code;
    private String desc;

    ReceiveAddressTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @JsonCreator
    public static ReceiveAddressTypeEnum toEnum(String code) {
        for (ReceiveAddressTypeEnum tmp : values()) {
            if (tmp.getCode().equals(code)) {
                return tmp;
            }
        }
        throw new IllegalStateException("ReceiveAddressTypeEnum-枚举" + code + "不存在");
    }
}
