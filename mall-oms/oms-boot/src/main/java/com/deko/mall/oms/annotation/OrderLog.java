package com.deko.mall.oms.annotation;

import java.lang.annotation.*;

/**
 * 订单日志注解
 * <p>
 * 用于标记需要记录操作日志的订单相关方法
 * </p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OrderLog {

    /**
     * 操作详情
     */
    String detail() default "";

    /**
     * 操作类型
     */
    String type() default "";

    /**
     * 业务单号，支持SpEL表达式
     */
    String bizNo() default "";
}
