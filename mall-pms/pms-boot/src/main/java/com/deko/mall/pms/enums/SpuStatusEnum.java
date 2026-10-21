package com.deko.mall.pms.enums;

import com.deko.mall.common.base.IBaseEnum;
import lombok.Getter;

public enum SpuStatusEnum implements IBaseEnum<Integer> {

    DRAFT(0, "操作"),
    // 上架
    ON_SALE(1, "上架"),
    // 下架
    OFF_SALE(2, "下架");


    @Getter
    private final Integer value;

    @Getter
    private final String label;

    SpuStatusEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}
