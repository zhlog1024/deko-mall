package com.deko.mall.pms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.deko.mall.common.base.IBaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 商品类型枚举
 */
@Getter
@AllArgsConstructor
public enum SpuTypeEnum implements IBaseEnum<String> {
    NORMAL("NORMAL", "普通商品");

    @EnumValue
    private final String value;

    private final String label;

}
