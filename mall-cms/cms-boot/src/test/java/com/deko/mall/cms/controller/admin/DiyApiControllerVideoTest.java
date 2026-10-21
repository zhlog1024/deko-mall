package com.deko.mall.cms.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deko.mall.cms.api.dto.VideoFeignPageQuery;
import com.deko.mall.cms.api.dto.VideoItemDTO;
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
 * 装修器视频取数通道单元测试（mall-cms plugins-video/list、plugins-video/auto-list → 同进程 VideoService）
 */
class DiyApiControllerVideoTest {

    private final VideoService videoService = Mockito.mock(VideoService.class);
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
                Mockito.mock(AskService.class),
                videoService,
                Mockito.mock(LiveRoomFeignClient.class)
        );
    }

    private VideoItemDTO sampleVideo(Long id, String title) {
        VideoItemDTO dto = new VideoItemDTO();
        dto.setId(id);
        dto.setTitle(title);
        dto.setCover("https://cdn.example.com/cover.png");
        dto.setVideoUrl("https://cdn.example.com/v.mp4");
        dto.setDuration(30);
        dto.setAccessCount(66);
        dto.setIsRecommended(1);
        dto.setIsHot(0);
        return dto;
    }

    @Test
    @DisplayName("plugins-video/list 按 ids 透传 mall-cms by-ids 回显")
    void pluginsVideoList_forwardsIdsToFeign() {
        VideoItemDTO dto = sampleVideo(1L, "测试视频");
        Mockito.when(videoService.listByIdsFeign(Mockito.anyList()))
                .thenReturn(List.of(dto));

        Result<List<VideoItemDTO>> result = controller.getPluginsVideoList(List.of(1L, 2L));

        // 同进程直调后入参即 ids 列表（不再是 VideoByIdsQuery 包装对象），断言语义不变：ids 原样透传
        ArgumentCaptor<List<Long>> captor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(videoService).listByIdsFeign(captor.capture());
        assertEquals(List.of(1L, 2L), captor.getValue(), "ids 应原样透传");
        assertNotNull(result);
        assertEquals("00000", result.getCode(), "应返回成功码");
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
        assertEquals("测试视频", result.getData().get(0).getTitle());
        assertEquals("https://cdn.example.com/v.mp4", result.getData().get(0).getVideoUrl());
    }

    @Test
    @DisplayName("plugins-video/auto-list 映射 keyword/推荐/热门/number→service page(仅启用),orderByType 映射 sortField,orderByRule 真实转发")
    void pluginsVideoAutoList_mapsToFeignPage() {
        VideoItemDTO dto = sampleVideo(2L, "热门视频");
        Page<VideoItemDTO> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(dto));
        Mockito.when(videoService.pageFeign(Mockito.any(VideoFeignPageQuery.class)))
                .thenReturn(page);

        // orderByType=1 → sortField=createTime(最新),rule=desc → 0
        Result<List<VideoItemDTO>> result = controller.getPluginsVideoAutoList("视频", 1, 1, 5, 1, "desc");

        ArgumentCaptor<VideoFeignPageQuery> captor = ArgumentCaptor.forClass(VideoFeignPageQuery.class);
        Mockito.verify(videoService).pageFeign(captor.capture());
        VideoFeignPageQuery sent = captor.getValue();
        assertEquals("视频", sent.getKeyword(), "keyword 应透传");
        assertEquals(1, sent.getIsRecommended(), "isRecommended 应透传");
        assertEquals(1, sent.getIsHot(), "isHot 应透传");
        assertEquals(5, sent.getPageSize(), "number 映射为 pageSize");
        assertEquals(1, sent.getPageNum(), "取第一页");
        assertEquals(1, sent.getIsEnable(), "仅取启用视频");
        assertEquals("createTime", sent.getSortField(), "orderByType=1 应映射 createTime");
        assertEquals(0, sent.getOrderByRule(), "orderByRule=desc 应归一为 0(降序)");
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
        assertEquals("热门视频", result.getData().get(0).getTitle());
    }

    @Test
    @DisplayName("plugins-video/auto-list 转发 orderByRule:asc/数值1 → 1(升序),desc/0/缺省 → 0(降序),综合不受规则影响仍不传 sortField")
    void pluginsVideoAutoList_forwardsOrderByRule() {
        Page<VideoItemDTO> emptyPage = new Page<>(1, 10, 0);
        emptyPage.setRecords(List.of());
        Mockito.when(videoService.pageFeign(Mockito.any(VideoFeignPageQuery.class)))
                .thenReturn(emptyPage);

        // orderByType=2(浏览) + asc → accessCount + 升序
        controller.getPluginsVideoAutoList(null, null, null, null, 2, "asc");
        // orderByType=3(时长) + 数值 "0" → duration + 降序
        controller.getPluginsVideoAutoList(null, null, null, null, 3, "0");
        // orderByType=1(最新) + 数值 "1" → createTime + 升序(兼容数值形态)
        controller.getPluginsVideoAutoList(null, null, null, null, 1, "1");
        // orderByType=0(综合) + asc → 不传 sortField,规则仍归一为 1
        controller.getPluginsVideoAutoList(null, null, null, null, 0, "asc");
        // 缺省 → 综合不传 sortField,规则缺省 0(降序)
        controller.getPluginsVideoAutoList(null, null, null, null, null, null);

        ArgumentCaptor<VideoFeignPageQuery> captor = ArgumentCaptor.forClass(VideoFeignPageQuery.class);
        Mockito.verify(videoService, Mockito.times(5)).pageFeign(captor.capture());
        assertEquals("accessCount", captor.getAllValues().get(0).getSortField(), "orderByType=2 应映射 accessCount");
        assertEquals(1, captor.getAllValues().get(0).getOrderByRule(), "asc 应归一为 1(升序)");
        assertEquals("duration", captor.getAllValues().get(1).getSortField(), "orderByType=3 应映射 duration");
        assertEquals(0, captor.getAllValues().get(1).getOrderByRule(), "数值 0 应归一为降序");
        assertEquals("createTime", captor.getAllValues().get(2).getSortField(), "orderByType=1 应映射 createTime");
        assertEquals(1, captor.getAllValues().get(2).getOrderByRule(), "数值 1 应归一为升序");
        assertNull(captor.getAllValues().get(3).getSortField(), "orderByType=0 应不传 sortField(综合)");
        assertEquals(1, captor.getAllValues().get(3).getOrderByRule(), "综合下规则仍归一透传为 1");
        assertNull(captor.getAllValues().get(4).getSortField(), "orderByType 缺省应不传 sortField");
        assertEquals(0, captor.getAllValues().get(4).getOrderByRule(), "规则缺省应默认 0(降序)");
    }

    @Test
    @DisplayName("plugins-video/auto-list 各 orderByType 映射 sortField(2浏览/3时长/0或缺省=综合不传)")
    void pluginsVideoAutoList_orderByTypeMapping() {
        Page<VideoItemDTO> emptyPage = new Page<>(1, 10, 0);
        emptyPage.setRecords(List.of());
        Mockito.when(videoService.pageFeign(Mockito.any(VideoFeignPageQuery.class)))
                .thenReturn(emptyPage);

        // orderByType=2 → accessCount
        controller.getPluginsVideoAutoList(null, null, null, null, 2, null);
        ArgumentCaptor<VideoFeignPageQuery> captor1 = ArgumentCaptor.forClass(VideoFeignPageQuery.class);
        Mockito.verify(videoService, Mockito.times(1)).pageFeign(captor1.capture());
        assertEquals("accessCount", captor1.getAllValues().get(0).getSortField(), "orderByType=2 应映射 accessCount");

        // orderByType=3 → duration
        controller.getPluginsVideoAutoList(null, null, null, null, 3, null);
        ArgumentCaptor<VideoFeignPageQuery> captor2 = ArgumentCaptor.forClass(VideoFeignPageQuery.class);
        Mockito.verify(videoService, Mockito.times(2)).pageFeign(captor2.capture());
        assertEquals("duration", captor2.getAllValues().get(1).getSortField(), "orderByType=3 应映射 duration");

        // orderByType=0(综合)/缺省 → sortField 不传(后端走默认 sort asc+创建时间倒序)
        controller.getPluginsVideoAutoList(null, null, null, null, 0, null);
        controller.getPluginsVideoAutoList(null, null, null, null, null, null);
        ArgumentCaptor<VideoFeignPageQuery> captor3 = ArgumentCaptor.forClass(VideoFeignPageQuery.class);
        Mockito.verify(videoService, Mockito.times(4)).pageFeign(captor3.capture());
        assertNull(captor3.getAllValues().get(2).getSortField(), "orderByType=0 应不传 sortField(综合)");
        assertNull(captor3.getAllValues().get(3).getSortField(), "orderByType 缺省应不传 sortField");
    }

    @Test
    @DisplayName("plugins-video/auto-list number 缺省或非正数时回退默认 pageSize")
    void pluginsVideoAutoList_defaultsPageSize() {
        Page<VideoItemDTO> page = new Page<>(1, 10, 0);
        page.setRecords(List.of());
        Mockito.when(videoService.pageFeign(Mockito.any(VideoFeignPageQuery.class)))
                .thenReturn(page);

        // number 为空(默认10)
        Result<List<VideoItemDTO>> emptyResult = controller.getPluginsVideoAutoList(null, null, null, null, null, null);
        ArgumentCaptor<VideoFeignPageQuery> captor = ArgumentCaptor.forClass(VideoFeignPageQuery.class);
        Mockito.verify(videoService, Mockito.times(1)).pageFeign(captor.capture());
        assertEquals(10, captor.getValue().getPageSize(), "number 缺省应回退 10");
        assertTrue(emptyResult.getData().isEmpty());

        // number 传 0(非法) 回退 10
        Mockito.when(videoService.pageFeign(Mockito.any(VideoFeignPageQuery.class)))
                .thenReturn(new Page<>(1, 10, 0));
        controller.getPluginsVideoAutoList(null, null, null, 0, null, null);
        ArgumentCaptor<VideoFeignPageQuery> captor2 = ArgumentCaptor.forClass(VideoFeignPageQuery.class);
        Mockito.verify(videoService, Mockito.times(2)).pageFeign(captor2.capture());
        assertEquals(10, captor2.getAllValues().get(1).getPageSize(), "number=0 应回退 10");
    }
}
