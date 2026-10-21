package com.deko.mall.common.web.i18n;

import com.deko.mall.common.i18n.I18nConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.List;
import java.util.Locale;

/**
 * 国际化语言配置
 * <p>
 * 使用 Spring 标准机制：
 * <ul>
 *   <li>{@link AcceptHeaderLocaleResolver} — 解析 Accept-Language 请求头</li>
 *   <li>{@link LocaleChangeInterceptor} — 支持 ?lang=zh_CN 参数切换语言</li>
 * </ul>
 */
@Slf4j
@Configuration
public class LocaleConfig {

    /**
     * 语言解析器：从 Accept-Language 请求头解析，默认简体中文
     */
    @Bean
    public LocaleResolver i18nLocaleResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(I18nConstants.DEFAULT_LOCALE);
        resolver.setSupportedLocales(List.of(
                Locale.SIMPLIFIED_CHINESE,
                Locale.US
        ));
        log.info("LocaleResolver 已配置，默认语言: {}", I18nConstants.DEFAULT_LOCALE);
        return resolver;
    }

    /**
     * 语言切换拦截器：通过 ?lang=en_US 切换语言
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName(I18nConstants.LANGUAGE_PARAM);
        return interceptor;
    }

    /**
     * 响应头过滤器：自动注入 Content-Language，告知客户端当前响应的语言
     */
    @Bean
    public FilterRegistrationBean<LocaleResponseFilter> localeResponseFilter() {
        FilterRegistrationBean<LocaleResponseFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new LocaleResponseFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
