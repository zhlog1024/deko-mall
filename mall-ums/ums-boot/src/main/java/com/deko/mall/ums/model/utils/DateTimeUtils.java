package com.deko.mall.ums.model.utils;

import java.time.LocalDateTime;
import java.time.YearMonth;

public class DateTimeUtils {

    /**
     * 获取指定月份的开始时间和结束时间
     *
     * @param year  年份（如 2023）
     * @param month 月份（1-12）
     * @return 包含两个元素的数组：[0]=当月开始时间, [1]=当月结束时间
     */
    public static LocalDateTime[] getMonthStartAndEnd(int year, int month) {
        // 验证月份有效性
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("月份必须在 1-12 之间");
        }

        YearMonth yearMonth = YearMonth.of(year, month);

        // 当月第一天 00:00:00
        LocalDateTime start = yearMonth.atDay(1)
                .atStartOfDay();

        // 当月最后一天 23:59:59.999999999
        LocalDateTime end = yearMonth.atEndOfMonth()
                .atTime(23, 59, 59, 999999999);

        return new LocalDateTime[]{start, end};
    }
}
