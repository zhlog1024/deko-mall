package com.deko.mall.common.web.i18n;

import com.deko.mall.common.i18n.I18nConstants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Feign 调用语言传递拦截器
 * <p>
 * 从当前请求的 {@link LocaleContextHolder} 读取语言环境，
 * 通过 X-Language header 传递给下游微服务。
 */
@Slf4j
@Component
public class FeignLanguageInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        try {
            Locale locale = LocaleContextHolder.getLocale();
            if (locale != null) {
                String language = locale.getLanguage() + "_" + locale.getCountry();
                template.header(I18nConstants.FEIGN_LANGUAGE_HEADER, language);
                log.debug("Feign 传递语言: {}, 目标: {}", language, template.path());
            }
        } catch (Exception e) {
            log.debug("Feign 语言传递失败", e);
        }
    }
}
