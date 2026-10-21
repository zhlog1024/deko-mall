package com.deko.mall.pms.service.impl;

import com.deko.mall.pms.mapper.PmsSkuMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SKU 关键字搜索测试：空关键字不查询，结果数量受默认值和上限约束。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SkuServiceImplTest {

    @InjectMocks
    private SkuServiceImpl skuService;

    @Mock
    private PmsSkuMapper pmsSkuMapper;

    @Test
    @DisplayName("关键字为空白：直接返回空集合，不查库")
    void searchSkuIds_blankKeyword_returnsEmpty() {
        assertTrue(skuService.searchSkuIds(null, 10).isEmpty());
        assertTrue(skuService.searchSkuIds("   ", 10).isEmpty());
        verify(pmsSkuMapper, never()).searchSkuIdsByNameOrSn(anyString(), anyInt());
    }

    @Test
    @DisplayName("关键字去空格后查库，返回命中的 SKU ID")
    void searchSkuIds_trimsAndQueries() {
        when(pmsSkuMapper.searchSkuIdsByNameOrSn(eq("原浆"), anyInt())).thenReturn(List.of(1754L, 1755L));

        assertEquals(List.of(1754L, 1755L), skuService.searchSkuIds(" 原浆 ", 10));
        verify(pmsSkuMapper).searchSkuIdsByNameOrSn(eq("原浆"), eq(10));
    }

    @Test
    @DisplayName("limit 为空/非正数时取默认 200")
    void searchSkuIds_defaultLimit() {
        ArgumentCaptor<Integer> captor = ArgumentCaptor.forClass(Integer.class);
        when(pmsSkuMapper.searchSkuIdsByNameOrSn(anyString(), anyInt())).thenReturn(List.of());

        skuService.searchSkuIds("原浆", null);
        skuService.searchSkuIds("原浆", 0);
        skuService.searchSkuIds("原浆", -5);

        verify(pmsSkuMapper, org.mockito.Mockito.times(3)).searchSkuIdsByNameOrSn(anyString(), captor.capture());
        assertEquals(List.of(200, 200, 200), captor.getAllValues());
    }

    @Test
    @DisplayName("limit 超过上限时夹到 1000（调用方不能要求无界查询）")
    void searchSkuIds_clampsMaxLimit() {
        when(pmsSkuMapper.searchSkuIdsByNameOrSn(anyString(), anyInt())).thenReturn(List.of());

        skuService.searchSkuIds("原浆", 100000);

        verify(pmsSkuMapper).searchSkuIdsByNameOrSn(eq("原浆"), eq(1000));
    }
}
