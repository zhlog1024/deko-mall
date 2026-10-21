package com.deko.mall.common.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 金额运算统一保留两位小数，采用 HALF_UP 舍入；空值按零处理。
 */
public final class AmountUtils {

    /** 金额精度：2 位小数 */
    public static final int SCALE = 2;

    /** 统一舍入模式：四舍五入 */
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private AmountUtils() {
    }

    /**
     * null 安全：null → 0
     */
    public static BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * 金额乘法：a * b，保留 2 位四舍五入
     */
    public static BigDecimal multiply(BigDecimal a, BigDecimal b) {
        return nvl(a).multiply(nvl(b)).setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * 金额除法：a / b，保留 2 位四舍五入；除数为 0 或 null 时返回 0
     */
    public static BigDecimal divide(BigDecimal a, BigDecimal b) {
        if (b == null || b.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(SCALE, ROUNDING_MODE);
        }
        return nvl(a).divide(b, SCALE, ROUNDING_MODE);
    }

    /**
     * 金额加法：a + b，保留 2 位四舍五入
     */
    public static BigDecimal add(BigDecimal a, BigDecimal b) {
        return nvl(a).add(nvl(b)).setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * 金额减法：a - b，保留 2 位四舍五入
     */
    public static BigDecimal subtract(BigDecimal a, BigDecimal b) {
        return nvl(a).subtract(nvl(b)).setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * 金额标准化：任意 BigDecimal → 2 位四舍五入
     */
    public static BigDecimal standardize(BigDecimal value) {
        return nvl(value).setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * 正数判断（&gt; 0）
     */
    public static boolean isPositive(BigDecimal value) {
        return nvl(value).compareTo(BigDecimal.ZERO) > 0;
    }
}
