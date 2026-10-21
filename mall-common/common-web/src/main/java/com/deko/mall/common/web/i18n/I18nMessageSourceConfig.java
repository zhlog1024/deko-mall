package com.deko.mall.common.web.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.Locale;

/**
 * 统一国际化消息源配置
 * <p>
 * 所有微服务共用此配置，各服务只需在 resources/i18n/messages*.properties 放置翻译文件即可。
 * classpath 隔离确保各服务读取自己的文件。
 */
@Configuration
public class I18nMessageSourceConfig {

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
        source.setBasenames("classpath:i18n/messages", "classpath:i18n/common_messages");
        source.setDefaultEncoding("UTF-8");
        source.setDefaultLocale(Locale.SIMPLIFIED_CHINESE);
        source.setCacheSeconds(300);
        source.setUseCodeAsDefaultMessage(true);
        return source;
    }
}
