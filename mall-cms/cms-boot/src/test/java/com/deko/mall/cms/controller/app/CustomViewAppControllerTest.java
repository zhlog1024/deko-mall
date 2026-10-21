package com.deko.mall.cms.controller.app;

import com.deko.mall.cms.service.CustomViewService;
import com.deko.mall.common.web.exception.BizException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 自定义页面 App 接口测试（standalone MockMvc,不启动 Spring 容器）
 * <p>
 * 覆盖: /{id}/web-page 官网整页渲染返回 text/html; 未启用/不存在 → 404 HTML 兜底页。
 */
@ExtendWith(MockitoExtension.class)
class CustomViewAppControllerTest {

    @Mock
    private CustomViewService customViewService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CustomViewAppController(customViewService)).build();
    }

    @Test
    @DisplayName("官网整页渲染: 返回 200 且 content-type 为 text/html")
    void webPage_ok() throws Exception {
        when(customViewService.getWebPageHtml(1L))
                .thenReturn("<!DOCTYPE html><html><head><title>关于我们</title></head><body>正文</body></html>");

        mockMvc.perform(get("/app-api/v1/custom-view/1/web-page"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("<!DOCTYPE html>")));
    }

    @Test
    @DisplayName("官网整页渲染: 未启用/不存在返回 404 HTML 兜底页")
    void webPage_notFound() throws Exception {
        when(customViewService.getWebPageHtml(99L))
                .thenThrow(new BizException("自定义页面已下架"));

        mockMvc.perform(get("/app-api/v1/custom-view/99/web-page"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("404")));
    }
}
