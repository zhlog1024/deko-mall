package com.deko.mall.common.i18n;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * 国际化工具类
 * <p>
 * Spring Bean，注入 MessageSource，通过 {@link LocaleContextHolder} 获取当前请求语言。
 * 同时提供静态桥接方法供 Result 等静态工具类使用。
 */
@Slf4j
@Component
public class I18nUtils {

    /** 静态桥接 — 由 Spring 容器构造时注入 */
    @Nullable
    private static volatile MessageSource staticMessageSource;

    private final MessageSource messageSource;

    public I18nUtils(MessageSource messageSource) {
        this.messageSource = messageSource;
        staticMessageSource = messageSource;
    }

    // 实例方法 (注入使用)

    public Locale currentLocale() {
        Locale locale = LocaleContextHolder.getLocale();
        return locale != null ? locale : I18nConstants.DEFAULT_LOCALE;
    }

    public String get(String key, Object... args) {
        return get(key, currentLocale(), args);
    }

    public String get(String key, Locale locale, Object... args) {
        if (StrUtil.isBlank(key)) return key;
        try {
            return messageSource.getMessage(key, args, locale);
        } catch (NoSuchMessageException e) {
            log.debug("i18n key not found: {}, locale: {}", key, locale);
            return key;
        }
    }

    public String get(String key, String defaultMessage, Object... args) {
        return get(key, defaultMessage, currentLocale(), args);
    }

    public String get(String key, String defaultMessage, Locale locale, Object... args) {
        if (StrUtil.isBlank(key)) return StrUtil.isNotBlank(defaultMessage) ? defaultMessage : key;
        try {
            return messageSource.getMessage(key, args, defaultMessage, locale);
        } catch (Exception e) {
            return StrUtil.isNotBlank(defaultMessage) ? defaultMessage : key;
        }
    }

    public String getError(String code, Object... args) {
        return get("error." + code, code, args);
    }

    public String getError(String code, String defaultMessage, Object... args) {
        return get("error." + code, defaultMessage, args);
    }

    public boolean hasKey(String key) {
        try {
            messageSource.getMessage(key, null, currentLocale());
            return true;
        } catch (NoSuchMessageException e) {
            return false;
        }
    }

    // 静态桥接 (供 Result 等静态工具使用)

    /**
     * 根据错误码解析国际化消息，找不到时返回默认消息。
     * 由 Result、BizException 等静态工具方法调用。
     */
    public static String resolveErrorCode(String code, String defaultMessage) {
        MessageSource ms = staticMessageSource;
        if (ms == null) return defaultMessage;
        Locale locale = LocaleContextHolder.getLocale();
        if (locale == null) locale = I18nConstants.DEFAULT_LOCALE;
        try {
            return ms.getMessage(code, null, defaultMessage, locale);
        } catch (NoSuchMessageException e) {
            return defaultMessage;
        }
    }
}
