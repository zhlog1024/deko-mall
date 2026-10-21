package com.deko.mall.oms.mapper;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.deko.mall.oms.model.query.OrderPageQuery;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrderQuerySqlTest {
    @Test
    void dynamicOrderQueriesCanBeParsedByTenantInterceptor() throws Exception {
        MybatisConfiguration configuration = new MybatisConfiguration();
        try (InputStream xml = getClass().getResourceAsStream("/mapper/OmsOrderMapper.xml")) {
            assertNotNull(xml);
            new XMLMapperBuilder(xml, configuration, "mapper/OmsOrderMapper.xml",
                    configuration.getSqlFragments()).parse();
        }
        TenantLineInnerInterceptor interceptor = new TenantLineInnerInterceptor(new TenantLineHandler() {
            @Override
            public Expression getTenantId() { return new LongValue(1); }
        });
        OrderPageQuery empty = new OrderPageQuery();
        OrderPageQuery filtered = new OrderPageQuery();
        filtered.setStatus(List.of(1, 2));
        filtered.setMemberId(1L);
        filtered.setSupplierId(1L);
        filtered.setKeywords("订单");
        filtered.setOrderType("NORMAL");
        filtered.setOrderScene("NORMAL");
        filtered.setExcludeOrderScenes(List.of("OTHER"));
        for (String method : List.of("getOrderPage", "listOrderPages", "getOrderPriceSum")) {
            for (OrderPageQuery query : List.of(empty, filtered)) {
                String sql = configuration.getMappedStatement("com.deko.mall.oms.mapper.OrderMapper." + method)
                        .getBoundSql(Map.of("queryParams", query)).getSql();
                assertDoesNotThrow(() -> interceptor.parserSingle(sql, null), method + "\n" + sql);
            }
        }
    }
}
