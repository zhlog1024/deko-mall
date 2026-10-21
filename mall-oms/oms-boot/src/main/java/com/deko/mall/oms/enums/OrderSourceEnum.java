package com.deko.mall.oms.enums;

import com.deko.mall.common.base.IBaseEnum;
import lombok.Getter;

/**
 * 订单来源枚举
 */

public enum OrderSourceEnum implements IBaseEnum<Integer> {

    APP(1, "APP"), // APP订单
    WEB(2, "WEB"), // 网页
    ;

    @Getter
    private Integer value;
    @Getter
    private String label;

    OrderSourceEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}
