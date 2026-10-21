package com.deko.mall.cms.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.deko.mall.common.web.exception.BizException;
import com.deko.mall.cms.mapper.DiyAppTabbarMapper;
import com.deko.mall.cms.model.entity.DiyAppTabbar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * DIY 底部导航（tabbar）本地表读写单元测试
 * <p>
 * 数据自治：底部导航装修配置存本服务本地表 cms_diy_app_tabbar（business=home 单行），
 * 不再依赖 mall-system sys_app_config / feign。
 */
class DiyAppTabbarServiceImplTest {

    private DiyAppTabbarMapper mapper;
    private DiyAppTabbarServiceImpl service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mapper = mock(DiyAppTabbarMapper.class);
        service = new DiyAppTabbarServiceImpl(objectMapper);
        try {
            java.lang.reflect.Field f = com.baomidou.mybatisplus.extension.service.impl.ServiceImpl.class
                    .getDeclaredField("baseMapper");
            f.setAccessible(true);
            f.set(service, mapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("读取：有配置时返回解析后的 content/style 对象")
    void getTabbarConfig_withConfig_returnsParsedObject() throws Exception {
        String configJson = "{\"content\":{\"nav_style\":\"0\",\"nav_type\":\"0\","
                + "\"nav_content\":[{\"id\":\"1\",\"name\":\"首页\"}]},"
                + "\"style\":{\"default_text_color\":\"#333333\"}}";
        DiyAppTabbar tabbar = new DiyAppTabbar();
        tabbar.setBusiness("home");
        tabbar.setConfig(configJson);
        // mybatis-plus 3.5.5: ServiceImpl.getOne(wrapper, throwEx) -> baseMapper.selectOne(Wrapper, boolean)
        when(mapper.selectOne(any(), anyBoolean())).thenReturn(tabbar);
        when(mapper.selectList(any())).thenReturn(java.util.List.of(tabbar));

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) service.getTabbarConfig("home");

        assertEquals("0", ((Map<?, ?>) data.get("content")).get("nav_style"));
        assertEquals("#333333", ((Map<?, ?>) data.get("style")).get("default_text_color"));
    }

    @Test
    @DisplayName("读取：business 缺省/空视为 home；无配置返回 null")
    void getTabbarConfig_emptyBusinessAndNoConfig_returnsNull() {
        when(mapper.selectOne(any(), anyBoolean())).thenReturn(null);
        when(mapper.selectList(any())).thenReturn(java.util.List.of());

        assertNull(service.getTabbarConfig(null), "business 为 null 应视为 home 正常读取");
        assertNull(service.getTabbarConfig(""), "business 为空应视为 home 正常读取");
    }

    @Test
    @DisplayName("保存：config 序列化 JSON 插入本地表（新行）")
    void saveTabbarConfig_insertNew() throws Exception {
        when(mapper.selectOne(any(), anyBoolean())).thenReturn(null);
        when(mapper.selectList(any())).thenReturn(java.util.List.of());
        when(mapper.insert(any(DiyAppTabbar.class))).thenReturn(1);

        Map<String, Object> config = Map.of(
                "content", Map.of("nav_style", "0", "nav_content", java.util.List.of()),
                "style", Map.of("default_text_color", "#333333"));

        service.saveTabbarConfig("home", config);

        verify(mapper).insert(any(DiyAppTabbar.class));
    }

    @Test
    @DisplayName("保存：已存在则更新本地表")
    void saveTabbarConfig_updateExisting() {
        DiyAppTabbar existing = new DiyAppTabbar();
        existing.setId(1L);
        existing.setBusiness("home");
        when(mapper.selectOne(any(), anyBoolean())).thenReturn(existing);
        when(mapper.selectList(any())).thenReturn(java.util.List.of(existing));
        when(mapper.updateById(any(DiyAppTabbar.class))).thenReturn(1);

        service.saveTabbarConfig("home", Map.of("content", Map.of()));

        verify(mapper).updateById(existing);
        assertEquals("{\"content\":{}}", existing.getConfig());
    }

    @Test
    @DisplayName("保存：config 为 null 拒绝（不改库）")
    void saveTabbarConfig_nullConfigRejected() {
        assertThrows(BizException.class, () -> service.saveTabbarConfig("home", null));
        verify(mapper, never()).insert(any());
        verify(mapper, never()).updateById(any());
    }
}
