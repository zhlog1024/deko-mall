package com.deko.mall.cms.service.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 文章列表排序映射单元测试（语义common_article_order_by_type_list）
 */
class ArticleSortSupportTest {

    @Test
    @DisplayName("0=综合排序 access_count,id")
    void resolveOrderColumnsComprehensive() {
        assertArrayEquals(new String[]{"access_count", "id"}, ArticleSortSupport.resolveOrderColumns(0));
    }

    @Test
    @DisplayName("1=人气排序 access_count")
    void resolveOrderColumnsAccess() {
        assertArrayEquals(new String[]{"access_count"}, ArticleSortSupport.resolveOrderColumns(1));
    }

    @Test
    @DisplayName("2=最新排序 id")
    void resolveOrderColumnsNewest() {
        assertArrayEquals(new String[]{"id"}, ArticleSortSupport.resolveOrderColumns(2));
    }

    @Test
    @DisplayName("null与非法值回退综合排序")
    void resolveOrderColumnsFallback() {
        assertArrayEquals(new String[]{"access_count", "id"}, ArticleSortSupport.resolveOrderColumns(null));
        assertArrayEquals(new String[]{"access_count", "id"}, ArticleSortSupport.resolveOrderColumns(99));
    }

    @Test
    @DisplayName("排序规则解析,非法默认desc")
    void resolveRule() {
        assertEquals("asc", ArticleSortSupport.resolveRule("asc"));
        assertEquals("asc", ArticleSortSupport.resolveRule("ASC"));
        assertEquals("desc", ArticleSortSupport.resolveRule("desc"));
        assertEquals("desc", ArticleSortSupport.resolveRule(null));
        assertEquals("desc", ArticleSortSupport.resolveRule("random"));
    }
}
