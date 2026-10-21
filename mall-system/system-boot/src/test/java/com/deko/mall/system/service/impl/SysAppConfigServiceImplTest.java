package com.deko.mall.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.deko.mall.system.mapper.SysAppConfigMapper;
import com.deko.mall.system.model.entity.SysAppConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * APP 配置按 key 读写单元测试
 * <p>
 * 通用配置读写能力（getConfigValue/saveConfigValue）：写入后必须清理 APP_CONFIG 缓存。
 * 注：DIY 装修数据(app_tabbar/cms_diy_home_mode)已归位 cms 本地表，本能力仅服务 App 基础配置。
 */
@SuppressWarnings("unchecked")
class SysAppConfigServiceImplTest {

    private SysAppConfigMapper mapper;
    private RedisTemplate<String, Object> redisTemplate;
    private SysAppConfigServiceImpl service;

    @BeforeEach
    void setUp() {
        mapper = mock(SysAppConfigMapper.class);
        redisTemplate = mock(RedisTemplate.class);
        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        service = new SysAppConfigServiceImpl(mapper, redisTemplate);
    }

    @Test
    @DisplayName("按 key 读取：存在返回 configValue")
    void getConfigValue_whenExists_returnsValue() {
        SysAppConfig config = new SysAppConfig();
        config.setConfigKey("app_tabbar");
        config.setConfigValue("{\"content\":{}}");
        when(mapper.selectOne(any(Wrapper.class))).thenReturn(config);

        String value = service.getConfigValue("app_tabbar");

        assertEquals("{\"content\":{}}", value);
    }

    @Test
    @DisplayName("按 key 读取：不存在返回 null")
    void getConfigValue_whenMissing_returnsNull() {
        when(mapper.selectOne(any(Wrapper.class))).thenReturn(null);

        assertNull(service.getConfigValue("app_tabbar"));
    }

    @Test
    @DisplayName("按 key 写入：已存在则更新并清理缓存")
    void saveConfigValue_whenExists_updatesAndClearsCache() {
        SysAppConfig config = new SysAppConfig();
        config.setId(44L);
        config.setConfigKey("app_tabbar");
        config.setConfigValue("old");
        when(mapper.selectOne(any(Wrapper.class))).thenReturn(config);
        when(mapper.updateById(any(SysAppConfig.class))).thenReturn(1);

        boolean ok = service.saveConfigValue("app_tabbar", "{\"content\":{\"nav_style\":\"0\"}}");

        assertTrue(ok);
        verify(mapper).updateById(config);
        assertEquals("{\"content\":{\"nav_style\":\"0\"}}", config.getConfigValue());
        verify(redisTemplate).delete("system:app:config");
    }

    @Test
    @DisplayName("按 key 写入：不存在则新建（json/base 分组）并清理缓存")
    void saveConfigValue_whenMissing_insertsAndClearsCache() {
        when(mapper.selectOne(any(Wrapper.class))).thenReturn(null);
        when(mapper.insert(any(SysAppConfig.class))).thenReturn(1);

        boolean ok = service.saveConfigValue("app_tabbar", "{\"content\":{}}");

        assertTrue(ok);
        verify(mapper).insert(any(SysAppConfig.class));
        verify(redisTemplate).delete("system:app:config");
    }

    @Test
    @DisplayName("写入异常时不触碰缓存删除逻辑以外逻辑（update 失败仍返回 true 与现有 saveConfigs 语义一致）")
    void saveConfigValue_updateFail_stillClearsCache() {
        SysAppConfig config = new SysAppConfig();
        config.setId(44L);
        when(mapper.selectOne(any(Wrapper.class))).thenReturn(config);
        when(mapper.updateById(any(SysAppConfig.class))).thenReturn(0);

        boolean ok = service.saveConfigValue("app_tabbar", "new");

        assertTrue(ok);
        verify(redisTemplate).delete("system:app:config");
    }

    @Test
    @DisplayName("clearCache 删除 APP_CONFIG 缓存 key")
    void clearCache_deletesKey() {
        service.clearCache();
        verify(redisTemplate).delete("system:app:config");
    }
}
