package com.deko.mall.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deko.mall.common.result.Result;
import com.deko.mall.common.web.exception.BizException;
import com.deko.mall.cms.mapper.DiyHomeModeMapper;
import com.deko.mall.cms.model.entity.DiyHomeMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * DIY 首页按端配置（cms_diy_home_mode 本地表）单元测试
 * <p>
 * 数据自治：首页按端(client_type)配置存本服务本地表 cms_diy_home_mode，
 * getAppHome 按端查行（定时窗口内）→ 页面启用锁定。
 */
@SuppressWarnings("unchecked")
class DiyHomeModeServiceImplTest {

    private DiyHomeModeMapper mapper;
    private DiyHomeModeServiceImpl service;

    @BeforeEach
    void setUp() {
        mapper = mock(DiyHomeModeMapper.class);
        service = new DiyHomeModeServiceImpl();
        try {
            java.lang.reflect.Field f = com.baomidou.mybatisplus.extension.service.impl.ServiceImpl.class
                    .getDeclaredField("baseMapper");
            f.setAccessible(true);
            f.set(service, mapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private DiyHomeMode mode(long diyPageId, String clientType) {
        DiyHomeMode m = new DiyHomeMode();
        m.setId(diyPageId);
        m.setClientType(clientType);
        m.setDiyPageId(diyPageId);
        return m;
    }

    private void stubOne(DiyHomeMode m) {
        // mybatis-plus 3.5.5: ServiceImpl.getOne(wrapper, throwEx) -> baseMapper.selectOne(Wrapper, boolean)（两参）
        when(mapper.selectOne(any(Wrapper.class), anyBoolean())).thenReturn(m);
        // getHomeModeConfigMap 走 selectList；selectOne(单参 default)在 3.5.5 仍可能被调，一并兜底
        when(mapper.selectOne(any(Wrapper.class))).thenReturn(m);
        when(mapper.selectList(any(Wrapper.class))).thenReturn(m == null ? new ArrayList<>() : List.of(m));
    }

    // getByClientTypeInWindow

    @Test
    @DisplayName("按端查询：未配置该端返回 null")
    void getByClientType_absentReturnsNull() {
        stubOne(null);
        assertNull(service.getByClientTypeInWindow("weixin"));
    }

    @Test
    @DisplayName("按端查询：定时区间内命中")
    void getByClientType_inWindowHit() {
        DiyHomeMode m = mode(12L, "weixin");
        m.setTimeStart(LocalDateTime.now().minusDays(1));
        m.setTimeEnd(LocalDateTime.now().plusDays(1));
        stubOne(m);

        DiyHomeMode hit = service.getByClientTypeInWindow("weixin");
        assertNotNull(hit);
        assertEquals(12L, hit.getDiyPageId());
    }

    @Test
    @DisplayName("按端查询：定时未开始返回 null")
    void getByClientType_timeStartFutureReturnsNull() {
        DiyHomeMode m = mode(12L, "weixin");
        m.setTimeStart(LocalDateTime.now().plusDays(1));
        stubOne(m);

        assertNull(service.getByClientTypeInWindow("weixin"));
    }

    @Test
    @DisplayName("按端查询：定时已结束返回 null")
    void getByClientType_timeEndPastReturnsNull() {
        DiyHomeMode m = mode(12L, "weixin");
        m.setTimeEnd(LocalDateTime.now().minusDays(1));
        stubOne(m);

        assertNull(service.getByClientTypeInWindow("weixin"));
    }

    // getHomeModeConfigMap

    @Test
    @DisplayName("全量读取：返回 {端: {diyId,timeStart,timeEnd}} 契约")
    void getHomeModeConfigMap_returnsContract() {
        DiyHomeMode wx = mode(12L, "weixin");
        when(mapper.selectList(any())).thenReturn(List.of(wx));

        Map<String, Object> map = service.getHomeModeConfigMap();
        assertNotNull(map.get("weixin"));
        Map<?, ?> item = (Map<?, ?>) map.get("weixin");
        assertEquals(12L, item.get("diyId"));
    }

    // saveHomeModeConfigMap

    @Test
    @DisplayName("保存：无有效端拒绝")
    void saveHomeModeConfigMap_noValidRejected() {
        assertThrows(BizException.class,
                () -> service.saveHomeModeConfigMap(Map.of("weixin", Map.of("diy_id", ""))));
    }

    @Test
    @DisplayName("保存：有效端 upsert 写入")
    void saveHomeModeConfigMap_success() {
        stubOne(null);
        when(mapper.insert(any(DiyHomeMode.class))).thenReturn(1);

        Map<String, Object> config = new HashMap<>();
        Map<String, Object> weixin = new HashMap<>();
        weixin.put("diy_id", 12);
        weixin.put("time_start", null);
        weixin.put("time_end", null);
        config.put("weixin", weixin);

        service.saveHomeModeConfigMap(config);

        verify(mapper).insert(any(DiyHomeMode.class));
    }

    @Test
    @DisplayName("保存：diy_id 非法拒绝")
    void saveHomeModeConfigMap_badDiyIdRejected() {
        assertThrows(BizException.class,
                () -> service.saveHomeModeConfigMap(Map.of("weixin", Map.of("diy_id", "abc"))));
    }
}
