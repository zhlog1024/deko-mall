package com.deko.mall.pay.api.enums;

import com.deko.mall.common.base.IBaseEnum;
import lombok.Getter;

@Getter
public enum PayChannelEnum implements IBaseEnum<String> {

    ALIPAY_WAP("ALIPAY_WAP", "支付宝WAP"),
    ALIPAY_APP("ALIPAY_APP", "支付宝APP"),
    WECHAT_JSAPI("WECHAT_JSAPI", "微信JSAPI"),
    WECHAT_APP("WECHAT_APP", "微信APP"),
    FULL_DEDUCTION("FULL_DEDUCTION", "全额抵扣"),
    OFFLINE_PAY("OFFLINE_PAY", "线下支付");

    @Getter
    private String value;
    @Getter
    private String label;

    PayChannelEnum(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public static PayChannelEnum getByValue(String value) {
        for (PayChannelEnum e : values()) {
            if (e.value.equals(value)) return e;
        }
        return null;
    }
}
