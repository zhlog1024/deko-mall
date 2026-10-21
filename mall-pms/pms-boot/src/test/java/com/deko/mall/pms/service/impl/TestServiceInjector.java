package com.deko.mall.pms.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;

import java.lang.reflect.Field;

/**
 * 测试工具：向 MyBatis-Plus ServiceImpl 注入 baseMapper（其 baseMapper 为 @Autowired protected 字段）
 */
public final class TestServiceInjector {

    private TestServiceInjector() {
    }

    public static void injectBaseMapper(IService<?> service, BaseMapper<?> mapper) {
        try {
            Field field = com.baomidou.mybatisplus.extension.service.impl.ServiceImpl.class
                    .getDeclaredField("baseMapper");
            field.setAccessible(true);
            field.set(service, mapper);
        } catch (Exception e) {
            throw new IllegalStateException("注入 baseMapper 失败", e);
        }
    }
}
