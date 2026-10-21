package com.deko.mall.ums.support;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;

/**
 * 测试工具：初始化 MyBatis-Plus 实体的 lambda 缓存
 * <p>
 * 纯单元测试（不启动 Spring）中使用 {@code LambdaUpdateWrapper.eq(UmsMember::getId, ...)} 时，
 * MyBatis-Plus 需要实体已被 {@code TableInfoHelper} 注册，否则报
 * {@code MybatisPlus can not find lambda cache for this entity}。此处手工注册。
 */
public final class TestTableInfoInitializer {

    private TestTableInfoInitializer() {
    }

    /**
     * 注册实体 TableInfo（幂等）
     *
     * @param entityClass 实体类
     */
    public static void init(Class<?> entityClass) {
        if (TableInfoHelper.getTableInfo(entityClass) != null) {
            return;
        }
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        assistant.setCurrentNamespace(entityClass.getName());
        TableInfoHelper.initTableInfo(assistant, entityClass);
    }

}
