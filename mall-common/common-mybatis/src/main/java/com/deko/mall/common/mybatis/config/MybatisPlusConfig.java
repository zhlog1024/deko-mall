package com.deko.mall.common.mybatis.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.deko.mall.common.mybatis.handler.*;
import com.deko.mall.common.security.util.SecurityUtils;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * mybatis-plus 配置类
 */
@Configuration
@EnableTransactionManagement
public class MybatisPlusConfig {

    /**
     * MyBatis Plus插件配置
     * 包括：租户隔离插件、数据权限插件、分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 多租户插件（用于供应商数据隔离）放在最前面
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler() {
            @Override
            public Expression getTenantId() {
                // 获取供应商ID，无论用户是什么角色
                // 实际是否执行租户过滤由ignoreTable方法决定
                if (SecurityUtils.isSupplier()) {
                    Long supplierId = SecurityUtils.getSupplierId();
                    if (supplierId != null) {
                        return new LongValue(supplierId);
                    }
                }
                // 返回一个特殊值，表示没有租户条件
                // 使用 -1 作为特殊标记，代表非供应商用户
                // 实际数据中不存在supplier_id为-1的记录，因此安全
                return new LongValue(-1);
            }

            @Override
            public String getTenantIdColumn() {
                return "supplier_id"; // 指定供应商ID字段名
            }

            @Override
            public boolean ignoreTable(String tableName) {
                // 非供应商用户或超级管理员，跳过所有表的租户过滤
                if (!SecurityUtils.isSupplier() || SecurityUtils.isRoot()) {
                    return true;
                }
                // 供应商用户根据表配置决定是否过滤
                return !TableFilterConfig.isSupplierTable(tableName);
            }
        }));

        // 数据权限插件（用于部门、用户数据权限）
        interceptor.addInnerInterceptor(new DataPermissionInterceptor(new MyDataPermissionHandler()));

        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));

        return interceptor;
    }

    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> {
            // 全局注册自定义TypeHandler
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            typeHandlerRegistry.register(String[].class, JdbcType.OTHER, StringArrayJsonTypeHandler.class);
            typeHandlerRegistry.register(Long[].class, JdbcType.OTHER, LongArrayJsonTypeHandler.class);
            typeHandlerRegistry.register(Integer[].class, JdbcType.OTHER, IntegerArrayJsonTypeHandler.class);
        };
    }

    /**
     * 自动填充数据库创建人、创建时间、更新人、更新时间
     */
    @Bean
    public GlobalConfig globalConfig() {
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setMetaObjectHandler(new MyMetaObjectHandler());
        return globalConfig;
    }

}
