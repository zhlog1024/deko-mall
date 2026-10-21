package com.deko.mall.cms.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deko.mall.cms.api.dto.AskFeignPageQuery;
import com.deko.mall.cms.api.dto.AskItemDTO;
import com.deko.mall.common.result.Result;
import com.deko.mall.cms.service.ArticleCategoryService;
import com.deko.mall.cms.service.ArticleService;
import com.deko.mall.cms.service.AskService;
import com.deko.mall.cms.service.DiyApiService;
import com.deko.mall.cms.service.DiyPageService;
import com.deko.mall.cms.service.VideoService;
import com.deko.mall.pms.api.BrandFeignClient;
import com.deko.mall.pms.api.CategoryFeignClient;
import com.deko.mall.pms.api.SpuFeignClient;
import com.deko.mall.sms.api.LiveRoomFeignClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 装修器问答取数通道单元测试（mall-cms ask/list、ask/auto-list → 同进程 AskService）
 */
class DiyApiControllerAskTest {

    private final AskService askService = Mockito.mock(AskService.class);
    private DiyApiController controller;

    @BeforeEach
    void setUp() {
        controller = new DiyApiController(
                Mockito.mock(DiyApiService.class),
                Mockito.mock(DiyPageService.class),
                Mockito.mock(BrandFeignClient.class),
                Mockito.mock(CategoryFeignClient.class),
                Mockito.mock(SpuFeignClient.class),
                Mockito.mock(ArticleService.class),
                Mockito.mock(ArticleCategoryService.class),
                askService,
                Mockito.mock(VideoService.class),
                Mockito.mock(LiveRoomFeignClient.class)
        );
    }

    private AskItemDTO sampleAsk(Long id, String title) {
        AskItemDTO dto = new AskItemDTO();
        dto.setId(id);
        dto.setTitle(title);
        dto.setSpuId(100L);
        dto.setSpuName("测试商品");
        dto.setIsReply(1);
        dto.setAccessCount(66);
        return dto;
    }

    @Test
    @DisplayName("ask/list 按 ids 透传 mall-cms by-ids 回显")
    void askList_forwardsIdsToFeign() {
        AskItemDTO dto = sampleAsk(1L, "这个商品保质期多久");
        Mockito.when(askService.listByIdsFeign(Mockito.anyList()))
                .thenReturn(List.of(dto));

        Result<List<AskItemDTO>> result = controller.getAskList(List.of(1L, 2L));

        // 同进程直调后入参即 ids 列表（不再是 AskByIdsQuery 包装对象），断言语义不变：ids 原样透传
        ArgumentCaptor<List<Long>> captor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(askService).listByIdsFeign(captor.capture());
        assertEquals(List.of(1L, 2L), captor.getValue(), "ids 应原样透传");
        assertNotNull(result);
        assertEquals("00000", result.getCode(), "应返回成功码");
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
        assertEquals("这个商品保质期多久", result.getData().get(0).getTitle());
    }

    @Test
    @DisplayName("ask/auto-list 映射 keyword/spuId/isReply/number→service page(仅启用,pageSize=number),orderByType/orderByRule 真实转发")
    void askAutoList_mapsToFeignPage() {
        AskItemDTO dto = sampleAsk(2L, "怎么退款");
        Page<AskItemDTO> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(dto));
        Mockito.when(askService.pageFeign(Mockito.any(AskFeignPageQuery.class))).thenReturn(page);

        Result<List<AskItemDTO>> result = controller.getAskAutoList("退款", null, 0, 5, 1, "asc");

        ArgumentCaptor<AskFeignPageQuery> captor = ArgumentCaptor.forClass(AskFeignPageQuery.class);
        Mockito.verify(askService).pageFeign(captor.capture());
        AskFeignPageQuery sent = captor.getValue();
        assertEquals("退款", sent.getKeyword(), "keyword 应透传");
        assertNull(sent.getSpuId(), "spuId 未传应为空");
        assertEquals(0, sent.getIsReply(), "isReply 应透传");
        assertEquals(5, sent.getPageSize(), "number 映射为 pageSize");
        assertEquals(1, sent.getPageNum(), "取第一页");
        assertEquals(1, sent.getIsEnable(), "仅取启用问答");
        assertEquals(1, sent.getOrderByType(), "orderByType=1(最新) 应转发");
        assertEquals(1, sent.getOrderByRule(), "orderByRule=asc 应归一为 1(升序)");
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
        assertEquals("怎么退款", result.getData().get(0).getTitle());
    }

    @Test
    @DisplayName("ask/auto-list 转发 orderByType/orderByRule:规则兼容 desc/asc/数值 0/1,缺省综合0+降序0")
    void askAutoList_forwardsOrderSorting() {
        Page<AskItemDTO> emptyPage = new Page<>(1, 10, 0);
        emptyPage.setRecords(List.of());
        Mockito.when(askService.pageFeign(Mockito.any(AskFeignPageQuery.class)))
                .thenReturn(emptyPage);

        // orderByType=1 + desc → 最新降序
        controller.getAskAutoList(null, null, null, null, 1, "desc");
        // orderByType=1 + 数值 "1" → 最新升序(兼容数值形态)
        controller.getAskAutoList(null, null, null, null, 1, "1");
        // orderByType=0(综合) + asc → 综合,规则仍透传归一为 1
        controller.getAskAutoList(null, null, null, null, 0, "asc");
        // 缺省 → orderByType 默认0(综合)、规则默认0(降序)
        controller.getAskAutoList(null, null, null, null, null, null);

        ArgumentCaptor<AskFeignPageQuery> captor = ArgumentCaptor.forClass(AskFeignPageQuery.class);
        Mockito.verify(askService, Mockito.times(4)).pageFeign(captor.capture());
        assertEquals(1, captor.getAllValues().get(0).getOrderByType(), "orderByType=1 应转发");
        assertEquals(0, captor.getAllValues().get(0).getOrderByRule(), "desc 应归一为 0(降序)");
        assertEquals(1, captor.getAllValues().get(1).getOrderByType(), "orderByType=1 应转发");
        assertEquals(1, captor.getAllValues().get(1).getOrderByRule(), "数值 1 应归一为升序");
        assertEquals(0, captor.getAllValues().get(2).getOrderByType(), "orderByType=0 应转发(综合)");
        assertEquals(1, captor.getAllValues().get(2).getOrderByRule(), "asc 应归一为 1(升序)");
        assertEquals(0, captor.getAllValues().get(3).getOrderByType(), "orderByType 缺省应默认 0(综合)");
        assertEquals(0, captor.getAllValues().get(3).getOrderByRule(), "orderByRule 缺省应默认 0(降序)");
    }

    @Test
    @DisplayName("ask/auto-list number 缺省或非正数时回退默认 pageSize")
    void askAutoList_defaultsPageSize() {
        Page<AskItemDTO> page = new Page<>(1, 10, 0);
        page.setRecords(List.of());
        Mockito.when(askService.pageFeign(Mockito.any(AskFeignPageQuery.class)))
                .thenReturn(page);

        // number 为空(默认10)
        Result<List<AskItemDTO>> emptyResult = controller.getAskAutoList(null, null, null, null, null, null);
        ArgumentCaptor<AskFeignPageQuery> captor = ArgumentCaptor.forClass(AskFeignPageQuery.class);
        Mockito.verify(askService, Mockito.times(1)).pageFeign(captor.capture());
        assertEquals(10, captor.getValue().getPageSize(), "number 缺省应回退 10");
        assertTrue(emptyResult.getData().isEmpty());

        // number 传 0(非法) 回退 10
        Mockito.when(askService.pageFeign(Mockito.any(AskFeignPageQuery.class)))
                .thenReturn(new Page<>(1, 10, 0));
        controller.getAskAutoList(null, null, null, 0, null, null);
        ArgumentCaptor<AskFeignPageQuery> captor2 = ArgumentCaptor.forClass(AskFeignPageQuery.class);
        Mockito.verify(askService, Mockito.times(2)).pageFeign(captor2.capture());
        assertEquals(10, captor2.getAllValues().get(1).getPageSize(), "number=0 应回退 10");
    }
}
