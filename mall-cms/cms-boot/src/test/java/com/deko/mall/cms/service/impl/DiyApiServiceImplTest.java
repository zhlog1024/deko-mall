package com.deko.mall.cms.service.impl;

import com.deko.mall.cms.model.vo.DiyConfigVO;
import com.deko.mall.cms.model.vo.ModuleGroupVO;
import com.deko.mall.cms.model.vo.ModuleVO;
import com.deko.mall.common.result.Result;
import com.deko.mall.system.api.SysAppConfigFeignClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 装修器组件列表单元测试
 * <p>
 * 背景：装修后台组件面板数据由后端 init 接口的 module_list 控制。
 * 店铺(shop)、直播列表/选项卡开放；门店组件依赖独立门店服务，本仓未包含。
 */
class DiyApiServiceImplTest {

    @SuppressWarnings("unchecked")
    private List<ModuleGroupVO> getModuleList() throws Exception {
        // getModuleList 不访问任何注入字段，CALLS_REAL_METHODS 无需构造参数
        DiyApiServiceImpl service = Mockito.mock(DiyApiServiceImpl.class, Mockito.CALLS_REAL_METHODS);
        Method method = DiyApiServiceImpl.class.getDeclaredMethod("getModuleList");
        method.setAccessible(true);
        return (List<ModuleGroupVO>) method.invoke(service);
    }

    @Test
    @DisplayName("组件面板插件分组开放店铺/直播列表/直播选项卡，分组展示名为高级")
    void moduleList_pluginsGroupAllOpened() throws Exception {
        List<ModuleGroupVO> groups = getModuleList();

        ModuleGroupVO pluginGroup = groups.stream()
                .filter(group -> "plugins".equals(group.getKey()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("插件分组不存在"));
        // 分组展示名：店铺/直播等插件组件归属"高级"分组（装修器左侧组件库标题）
        assertEquals("高级", pluginGroup.getName(), "插件分组展示名应为高级");
        List<String> pluginKeys = pluginGroup.getData().stream()
                .map(ModuleVO::getKey)
                .toList();

        assertEquals(List.of("shop", "plugins-live", "plugins-live-tabs"), pluginKeys,
                "插件分组应含店铺/直播列表/直播选项卡（不含门店），当前包含: " + pluginKeys);
    }

    @Test
    @DisplayName("基础组件与工具组件分组不受影响")
    void moduleList_baseAndToolGroupsStillAvailable() throws Exception {
        List<ModuleGroupVO> groups = getModuleList();

        List<String> baseKeys = groups.stream()
                .filter(group -> "base".equals(group.getKey()))
                .flatMap(group -> group.getData().stream())
                .map(ModuleVO::getKey)
                .toList();
        List<String> toolKeys = groups.stream()
                .filter(group -> "tool".equals(group.getKey()))
                .flatMap(group -> group.getData().stream())
                .map(ModuleVO::getKey)
                .toList();

        assertFalse(baseKeys.isEmpty(), "基础组件分组不应为空");
        assertFalse(toolKeys.isEmpty(), "工具组件分组不应为空");
        assertTrue(baseKeys.contains("goods-list"), "商品列表组件应保留");
        assertTrue(baseKeys.contains("goods-magic"), "商品魔方组件应注册");
        assertTrue(toolKeys.contains("title"), "标题组件应保留");
    }

    @Test
    @DisplayName("基础组件分组含问答列表与问答选项卡")
    void moduleList_askComponentsRegistered() throws Exception {
        List<ModuleGroupVO> groups = getModuleList();

        List<String> baseKeys = groups.stream()
                .filter(group -> "base".equals(group.getKey()))
                .flatMap(group -> group.getData().stream())
                .map(ModuleVO::getKey)
                .toList();

        assertTrue(baseKeys.contains("ask"), "基础组件应注册 ask(问答),当前包含: " + baseKeys);
        assertTrue(baseKeys.contains("ask-tabs"), "基础组件应注册 ask-tabs(问答选项卡),当前包含: " + baseKeys);
        // 沿用 article-list/article-tabs 双开的注册位置(紧随其后)
        assertTrue(baseKeys.indexOf("ask") > baseKeys.indexOf("article-tabs"),
                "ask 应注册在 article-tabs 之后");
        assertTrue(baseKeys.indexOf("ask-tabs") > baseKeys.indexOf("ask"),
                "ask-tabs 应注册在 ask 之后");
    }

    /**
     * 反射调用私有 buildConfig()（不依赖任何注入字段，只有 getConfigValue 占位实现）
     */
    private DiyConfigVO buildConfig(DiyApiServiceImpl service) throws Exception {
        Method method = DiyApiServiceImpl.class.getDeclaredMethod("buildConfig");
        method.setAccessible(true);
        return (DiyConfigVO) method.invoke(service);
    }

    @Test
    @DisplayName("装修器预览地址默认指向示例用户端 H5 域名")
    void buildConfig_previewUrlUsesDefaultH5Domain() throws Exception {
        // 走真实构造器，确保 @Value 字段初始化值生效（Mockito mock 不执行字段初始化）
        DiyApiServiceImpl service = new DiyApiServiceImpl(null, null, null, null, null);

        DiyConfigVO config = buildConfig(service);

        assertNotNull(config.getPreviewUrl(), "预览地址不能为空");
        assertTrue(config.getPreviewUrl().startsWith("https://m.example.com"),
                "预览地址应指向示例用户端 H5 m.example.com，当前: " + config.getPreviewUrl());
    }

    @Test
    @DisplayName("装修器预览地址可由 diy.preview-url 覆盖（Nacos 注入字段）")
    void buildConfig_previewUrlOverridable() throws Exception {
        DiyApiServiceImpl service = new DiyApiServiceImpl(null, null, null, null, null);
        String override = "https://m.example.com/#/pages/diy/diy";
        Field field = DiyApiServiceImpl.class.getDeclaredField("previewUrl");
        field.setAccessible(true);
        field.set(service, override);

        DiyConfigVO config = buildConfig(service);

        assertEquals(override, config.getPreviewUrl(), "预览地址应可被配置覆盖");
    }

    // 验证系统配置读取与默认值回退。

    private DiyApiServiceImpl withConfigClient(SysAppConfigFeignClient client) {
        return new DiyApiServiceImpl(null, null, null, client, null);
    }

    private String invokeGetConfigValue(DiyApiServiceImpl service, String key, String def) throws Exception {
        Method m = DiyApiServiceImpl.class.getDeclaredMethod("getConfigValue", String.class, String.class);
        m.setAccessible(true);
        return (String) m.invoke(service, key, def);
    }

    @Test
    @DisplayName("getConfigValue：Feign 返回成功时取配置中心的值，而非硬编码默认值")
    void getConfigValue_returnsValueFromConfigCenter() throws Exception {
        SysAppConfigFeignClient client = Mockito.mock(SysAppConfigFeignClient.class);
        Mockito.when(client.getConfigValue("file.attachment.host"))
                .thenReturn(Result.success("http://minio:9000/deko-mall"));

        String actual = invokeGetConfigValue(withConfigClient(client), "file.attachment.host", "https://fallback");

        assertEquals("http://minio:9000/deko-mall", actual, "应优先使用 sys_app_config 中的值");
    }

    @Test
    @DisplayName("getConfigValue：配置值为空时回退默认值")
    void getConfigValue_fallsBackWhenBlank() throws Exception {
        SysAppConfigFeignClient client = Mockito.mock(SysAppConfigFeignClient.class);
        Mockito.when(client.getConfigValue("site.logo")).thenReturn(Result.success(""));

        String actual = invokeGetConfigValue(withConfigClient(client), "site.logo", "https://default-logo");

        assertEquals("https://default-logo", actual, "空值应回退默认值");
    }

    @Test
    @DisplayName("getConfigValue：Feign 抛异常时回退默认值，不向上冒泡（装修接口不能因此 500）")
    void getConfigValue_swallowsExceptionAndFallsBack() throws Exception {
        SysAppConfigFeignClient client = Mockito.mock(SysAppConfigFeignClient.class);
        Mockito.when(client.getConfigValue("site.name")).thenThrow(new RuntimeException("feign timeout"));

        String actual = invokeGetConfigValue(withConfigClient(client), "site.name", "商城");

        assertEquals("商城", actual, "Feign 失败应回退默认值，不能抛异常");
    }

    @Test
    @DisplayName("getConfigValue：配置客户端为 null（脱离 Spring 容器）时也安全回退")
    void getConfigValue_handlesNullClient() throws Exception {
        String actual = invokeGetConfigValue(withConfigClient(null), "site.name", "商城");

        assertEquals("商城", actual, "无 Feign 客户端时应安全回退");
    }
}
