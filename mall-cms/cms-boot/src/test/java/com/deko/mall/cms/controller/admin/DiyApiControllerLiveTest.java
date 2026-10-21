package com.deko.mall.cms.controller.admin;

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
import com.deko.mall.sms.api.dto.LiveRoomItemDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 装修器直播取数通道单元测试
 * <p>
 * 覆盖：{@code /api/v1/diy-designer/live/auto-list} → {@link LiveRoomFeignClient#diyList}
 * 的参数透传（关键词 / 多状态逗号串 / 推荐 / 条数）与结果透传。
 * 依赖方为 mall-sms {@code /feign-api/v1/live/diy-list}。
 */
class DiyApiControllerLiveTest {

    private final LiveRoomFeignClient liveRoomFeignClient = Mockito.mock(LiveRoomFeignClient.class);
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
                Mockito.mock(VideoService.class),
                liveRoomFeignClient
        );
    }

    private LiveRoomItemDTO sampleRoom(Long id, String title, Integer status, String statusName) {
        LiveRoomItemDTO dto = new LiveRoomItemDTO();
        dto.setId(id);
        dto.setTitle(title);
        dto.setCover("https://cdn.example.com/live.png");
        dto.setSellerName("张老师");
        dto.setStatus(status);
        dto.setStatusName(statusName);
        dto.setOnlineCount(128);
        dto.setUrl("/pages/plugins/live/detail/detail?id=" + id);
        return dto;
    }

    @Test
    @DisplayName("直播自动数据：参数原样透传给 mall-sms，结果原样返回")
    void liveAutoList_shouldForwardParamsAndResult() {
        List<LiveRoomItemDTO> rows = List.of(
                sampleRoom(1L, "直播测试", 1, "直播中"),
                sampleRoom(2L, "直播测试2", 0, "未开播")
        );
        when(liveRoomFeignClient.diyList(any(), any(), any(), any())).thenReturn(Result.success(rows));

        Result<List<LiveRoomItemDTO>> result =
                controller.getLiveAutoList("养生", "0,1", 1, 6);

        ArgumentCaptor<String> keywordsCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> statusCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> recommendedCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> numberCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(liveRoomFeignClient).diyList(
                keywordsCaptor.capture(), statusCaptor.capture(), recommendedCaptor.capture(), numberCaptor.capture());

        assertEquals("养生", keywordsCaptor.getValue());
        // 多状态必须保持逗号串原样透传（后端按 IN 解析），不能被改写
        assertEquals("0,1", statusCaptor.getValue());
        assertEquals(1, recommendedCaptor.getValue());
        assertEquals(6, numberCaptor.getValue());

        assertEquals(2, result.getData().size());
        assertEquals("直播测试", result.getData().get(0).getTitle());
        assertEquals("直播中", result.getData().get(0).getStatusName());
        assertEquals("张老师", result.getData().get(0).getSellerName());
        assertTrue(result.getData().get(0).getUrl().startsWith("/pages/plugins/live/detail/detail?id="));
    }

    @Test
    @DisplayName("直播自动数据：筛选项为空时也照常透传（空串/ null 表示不筛选）")
    void liveAutoList_shouldForwardBlankFilters() {
        when(liveRoomFeignClient.diyList(eq(""), eq(""), eq(null), eq(10)))
                .thenReturn(Result.success(List.of()));

        Result<List<LiveRoomItemDTO>> result =
                controller.getLiveAutoList("", "", null, 10);

        assertEquals(0, result.getData().size());
    }
}
