package com.deko.mall.common.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Common核心模块自动配置
 */
@Configuration
@ComponentScan({"com.deko.mall.common.utils"})
public class CommonCoreAutoConfiguration {
}
