package com.deko.mall.common.mybatis.config;

import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 数据权限表过滤配置
 */
@Component
public class TableFilterConfig {

    /**
     * 需要进行供应商过滤的表
     */
    private static final List<String> SUPPLIER_TABLES = Arrays.asList(
            "pms_spu",
            "pms_sku",
            "pms_freight_template",
            "pms_forbidden_area",
            "oms_order_item",
            "pms_supplier_asset",
            "pms_supplier_asset_record",
            "pms_supplier_settlement",
            "pms_supplier_withdrawal"
    );

    /**
     * 判断表是否需要供应商过滤
     */
    public static boolean isSupplierTable(String tableName) {
        if (StrUtil.isBlank(tableName)) {
            return false;
        }

        // 表名可能带有schema前缀，如mall.pms_spu
        String actualTableName = tableName;
        int dotIndex = tableName.lastIndexOf('.');
        if (dotIndex > 0) {
            actualTableName = tableName.substring(dotIndex + 1);
        }

        return SUPPLIER_TABLES.contains(actualTableName.toLowerCase());
    }
}
