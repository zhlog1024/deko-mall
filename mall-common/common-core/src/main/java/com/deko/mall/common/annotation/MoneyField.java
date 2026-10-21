package com.deko.mall.common.annotation;

import java.lang.annotation.*;

/**
 * 金额字段注解，用于标记需要自动转换单位的金额字段
 * 后端存储：Long类型，以分为单位
 * 前端展示：String类型，以元为单位，如："99.99"
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MoneyField {

    /**
     * 是否添加货币符号，如 "¥ 99.99"
     */
    boolean withSymbol() default false;

    /**
     * 货币符号，默认人民币符号
     */
    String symbol() default "¥";

    /**
     * 是否添加货币单位，如 "99.99元"
     */
    boolean withUnit() default false;

    /**
     * 货币单位，如元、美元等
     */
    String unit() default "元";

    /**
     * 小数点后位数，默认2位
     */
    int scale() default 2;
}
