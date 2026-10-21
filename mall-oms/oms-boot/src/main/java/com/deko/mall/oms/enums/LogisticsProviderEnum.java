package com.deko.mall.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.deko.mall.common.base.IBaseEnum;
import lombok.Getter;

/**
 * 物流供应商枚举
 */
@Getter
public enum LogisticsProviderEnum implements IBaseEnum<String> {

    KUAIDI100("kuaidi100", "快递100"),
    WX_MINIAPP("wx_miniapp", "微信小程序"),
    // 预留其他供应商扩展
    ;

    @EnumValue
    @JsonValue
    private final String value;
    private final String label;

    LogisticsProviderEnum(String value, String label) {
        this.value = value;
        this.label = label;
    }
}
