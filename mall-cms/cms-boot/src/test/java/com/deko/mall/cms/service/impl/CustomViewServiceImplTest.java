package com.deko.mall.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.deko.mall.cms.model.entity.CustomView;
import com.deko.mall.common.web.exception.BizException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

/**
 * 自定义页面服务单元测试（mock mapper 边界,覆盖官网整页渲染）
 * <p>
 * 覆盖: web-page 整页 HTML 拼装(style/body/script/SEO)、SEO 缺省回退与转义、
 * 访问量自增、未启用/逻辑删除异常。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CustomViewServiceImplTest {

    private CustomViewServiceImpl customViewService;

    @BeforeEach
    void setUp() {
        customViewService = Mockito.spy(new CustomViewServiceImpl());
        // 桩:写库方法(单测边界,真实 DB 交互不在此验证)
        doReturn(true).when(customViewService).update(any(Wrapper.class));
        // 桩:getById 兜底(各测试按需覆盖)
        doReturn(null).when(customViewService).getById(any());
    }

    private CustomView buildView(Long id, Integer isEnable, Integer deleted) {
        CustomView view = new CustomView();
        view.setId(id);
        view.setName("关于我们");
        view.setLogo("https://cdn.example.com/logo.png");
        view.setHtmlContent("<p>官网正文内容</p>");
        view.setCssContent("body { color: #333; }");
        view.setJsContent("console.log('custom view page');");
        view.setIsEnable(isEnable);
        view.setIsHeader(1);
        view.setIsFooter(1);
        view.setIsFullScreen(0);
        view.setAccessCount(10);
        view.setDeleted(deleted);
        return view;
    }

    // 官网整页渲染

    @Test
    @DisplayName("官网整页渲染: style/html/script 与 SEO head 顺序正确,访问量+1")
    void webPageHtml_fullAssembly() {
        CustomView view = buildView(1L, 1, 0);
        doReturn(view).when(customViewService).getById(1L);

        String doc = customViewService.getWebPageHtml(1L);

        assertTrue(doc.startsWith("<!DOCTYPE html>"), "应以 DOCTYPE 开头");
        assertTrue(doc.contains("<title>关于我们</title>"), "seoTitle 缺省应回退页面名称");
        // 顺序: style 在 head, html 进 body, script 在正文后
        assertTrue(doc.indexOf("<style>") < doc.indexOf("</style>"), "css 应包进 style");
        assertTrue(doc.indexOf("</style>") < doc.indexOf("<body>"), "style 应在 body 前(head)");
        assertTrue(doc.indexOf("<p>官网正文内容</p>") > doc.indexOf("<body>"), "html 应输出到 body");
        assertTrue(doc.indexOf("<script>") > doc.indexOf("<p>官网正文内容</p>"), "js 应在正文后输出");
        // 访问量自增(SQL 原子更新)
        verify(customViewService).update(any(Wrapper.class));
    }

    @Test
    @DisplayName("官网整页渲染: SEO 标题关键字描述均输出且 HTML 转义")
    void webPageHtml_seoEscaped() {
        CustomView view = buildView(2L, 1, 0);
        view.setSeoTitle("品牌\"\"<h1>标题</h1>");
        view.setSeoKeywords("健康,\"滋补\"<b>");
        view.setSeoDesc("演示商城&健康公社");
        doReturn(view).when(customViewService).getById(2L);

        String doc = customViewService.getWebPageHtml(2L);

        assertTrue(doc.contains("&lt;h1&gt;"), "标题应转义 < >");
        assertTrue(doc.contains("&quot;滋补&quot;&lt;b&gt;"), "关键字应转义引号与尖括号");
        assertTrue(doc.contains("演示商城&amp;健康公社"), "描述应转义 &");
    }

    @Test
    @DisplayName("官网整页渲染: 未启用页面抛 BizException(下架)")
    void webPageHtml_disabled() {
        CustomView view = buildView(3L, 0, 0);
        doReturn(view).when(customViewService).getById(3L);

        BizException ex = assertThrows(BizException.class, () -> customViewService.getWebPageHtml(3L));
        assertTrue(ex.getMessage().contains("下架"), "未启用提示应含下架: " + ex.getMessage());
    }

    @Test
    @DisplayName("官网整页渲染: 逻辑删除页面抛 BizException(不存在)")
    void webPageHtml_deleted() {
        CustomView view = buildView(4L, 1, 1);
        doReturn(view).when(customViewService).getById(4L);

        BizException ex = assertThrows(BizException.class, () -> customViewService.getWebPageHtml(4L));
        assertTrue(ex.getMessage().contains("不存在"), "逻辑删除提示应含不存在: " + ex.getMessage());
    }

    @Test
    @DisplayName("官网整页渲染: 页面不存在抛 BizException")
    void webPageHtml_missing() {
        BizException ex = assertThrows(BizException.class, () -> customViewService.getWebPageHtml(99L));
        assertTrue(ex.getMessage().contains("不存在"), "空数据提示应含不存在: " + ex.getMessage());
    }
}
