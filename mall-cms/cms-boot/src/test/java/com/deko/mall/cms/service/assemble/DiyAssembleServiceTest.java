package com.deko.mall.cms.service.assemble;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.deko.mall.cms.api.dto.ArticleAutoQuery;
import com.deko.mall.cms.api.dto.ArticleItemDTO;
import com.deko.mall.cms.api.dto.AskFeignPageQuery;
import com.deko.mall.cms.api.dto.AskItemDTO;
import com.deko.mall.cms.api.dto.VideoFeignPageQuery;
import com.deko.mall.cms.api.dto.VideoItemDTO;
import com.deko.mall.cms.service.ArticleService;
import com.deko.mall.cms.service.AskService;
import com.deko.mall.cms.service.VideoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * DIY 服务端装配层单测
 * <p>
 * 覆盖：ask/plugins-video/article 的 auto(data_type=1) 装配覆盖 data_auto_list、
 * 指定模式(data_type=0)不动、无 key/非装配 key 不触 Feign。
 */
class DiyAssembleServiceTest {

    private DiyAssembleService service;

    @Mock
    private AskService askService;
    @Mock
    private VideoService videoService;
    @Mock
    private ArticleService articleService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new DiyAssembleService(askService, videoService, articleService);
    }

    private ObjectNode configWith(String key, String dataType) {
        ObjectNode config = objectMapper.createObjectNode();
        ObjectNode diyItem = objectMapper.createObjectNode();
        diyItem.put("key", key);
        diyItem.put("id", "1");
        ObjectNode comData = objectMapper.createObjectNode();
        ObjectNode content = objectMapper.createObjectNode();
        content.put("data_type", dataType);
        content.put("number", 5);
        content.put("keywords", "测试");
        comData.set("content", content);
        diyItem.set("com_data", comData);
        config.withArray("diy_data").add(diyItem);
        config.withArray("tabs_data");
        return config;
    }

    private ObjectNode configWithTabs(String rootKey, String tabDataType) {
        // 顶层 tabs 组件：content.tabs_list[] 每 tab 独立 data_type（对齐 ask-tabs/plugins-video-tabs）
        ObjectNode config = objectMapper.createObjectNode();
        ObjectNode tabsItem = objectMapper.createObjectNode();
        tabsItem.put("key", rootKey);
        tabsItem.put("id", "t1");
        ObjectNode comData = objectMapper.createObjectNode();
        ObjectNode content = objectMapper.createObjectNode();
        content.put("tabs_active_index", 0);
        ObjectNode tab = objectMapper.createObjectNode();
        tab.put("id", "tab-1");
        tab.put("data_type", tabDataType);
        tab.put("number", 3);
        tab.put("is_reply", "1");
        content.withArray("tabs_list").add(tab);
        comData.set("content", content);
        tabsItem.set("com_data", comData);
        config.withArray("diy_data");
        config.withArray("tabs_data").add(tabsItem);
        return config;
    }

    @Test
    @DisplayName("ask auto 模式：按筛选条件经 Feign 拉取并覆盖 data_auto_list（渲染行结构）")
    void assemble_askAutoFillsDataAutoList() {
        ObjectNode config = configWith("ask", "1");
        AskItemDTO dto = new AskItemDTO();
        dto.setId(10L);
        dto.setTitle("问题标题");
        dto.setIsReply(1);
        dto.setAccessCount(3);
        dto.setCreateTime(LocalDateTime.of(2026, 9, 1, 10, 30));
        when(askService.pageFeign(any(AskFeignPageQuery.class))).thenReturn(pageOf(dto));
        service.assemble(config);
        verify(askService).pageFeign(any(AskFeignPageQuery.class));
        ObjectNode content = (ObjectNode) config.withArray("diy_data").get(0).path("com_data").path("content");
        assertEquals(1, content.withArray("data_auto_list").size());
        assertEquals("问题标题", content.withArray("data_auto_list").get(0).path("title").asText());
        assertEquals("/pages/plugins/ask/detail/detail?id=10", content.withArray("data_auto_list").get(0).path("url").asText());
    }

    @Test
    @DisplayName("指定模式(data_type=0)：不调 Feign、data_auto_list 不动")
    void assemble_appointModeDoesNotTouch() {
        ObjectNode config = configWith("ask", "0");
        service.assemble(config);
        verify(askService, never()).pageFeign(any());
        ObjectNode content = (ObjectNode) config.withArray("diy_data").get(0).path("com_data").path("content");
        assertTrue(content.withArray("data_auto_list").isEmpty());
    }

    @Test
    @DisplayName("plugins-video auto：覆盖 data_auto_list 且 URL 拼详情页")
    void assemble_videoAutoFills() {
        ObjectNode config = configWith("plugins-video", "1");
        VideoItemDTO dto = new VideoItemDTO();
        dto.setId(20L);
        dto.setTitle("视频标题");
        dto.setCover("http://img/1.jpg");
        dto.setAccessCount(9);
        when(videoService.pageFeign(any(VideoFeignPageQuery.class))).thenReturn(pageOf(dto));
        service.assemble(config);
        ObjectNode content = (ObjectNode) config.withArray("diy_data").get(0).path("com_data").path("content");
        assertEquals(1, content.withArray("data_auto_list").size());
        assertEquals("/pages/plugins/video/detail/detail?id=20", content.withArray("data_auto_list").get(0).path("url").asText());
    }

    @Test
    @DisplayName("article auto：经 ArticleService.autoListFeign 拉取覆盖")
    void assemble_articleAutoFills() {
        ObjectNode config = configWith("article-list", "1");
        ArticleItemDTO dto = new ArticleItemDTO();
        dto.setId(30L);
        dto.setTitle("文章标题");
        when(articleService.autoListFeign(any(ArticleAutoQuery.class))).thenReturn(List.of(dto));
        service.assemble(config);
        ObjectNode content = (ObjectNode) config.withArray("diy_data").get(0).path("com_data").path("content");
        assertEquals(1, content.withArray("data_auto_list").size());
        assertEquals("文章标题", content.withArray("data_auto_list").get(0).path("title").asText());
    }

    @Test
    @DisplayName("tabs 版(ask-tabs)：遍历 tabs_list 每 tab 独立装配")
    void assemble_tabsEachTab() {
        ObjectNode config = configWithTabs("ask-tabs", "1");
        AskItemDTO dto = new AskItemDTO();
        dto.setId(40L);
        dto.setTitle("Tab问题");
        when(askService.pageFeign(any(AskFeignPageQuery.class))).thenReturn(pageOf(dto));
        service.assemble(config);
        ObjectNode tab = (ObjectNode) config.withArray("tabs_data").get(0).path("com_data").path("content").path("tabs_list").get(0);
        assertEquals(1, tab.withArray("data_auto_list").size());
    }

    @Test
    @DisplayName("非装配 key(goods-list 等)：不触下游取数（App 端有运行时兜底）")
    void assemble_unknownKeySkips() {
        ObjectNode config = configWith("goods-list", "1");
        service.assemble(config);
        verify(askService, never()).pageFeign(any());
        verify(videoService, never()).pageFeign(any());
        verify(articleService, never()).autoListFeign(any());
    }

    @Test
    @DisplayName("config 为 null/空：不抛异常")
    void assemble_nullSafe() {
        service.assemble(null);
        service.assemble(objectMapper.createObjectNode());
    }

    /**
     * 构造 Service 层返回的分页对象（原 Feign 层返回 PageResult，同进程直调后为 IPage）。
     * 保持 total=1 与单条 records，语义与改造前一致。
     */
    private <T> Page<T> pageOf(T dto) {
        Page<T> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(dto));
        return page;
    }
}
