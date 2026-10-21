package com.deko.mall.common.i18n;

import java.util.Locale;

/**
 * 国际化常量
 */
public interface I18nConstants {

    /** 默认语言 */
    Locale DEFAULT_LOCALE = Locale.SIMPLIFIED_CHINESE;

    /** 支持的语言（BCP 47 标准格式） */
    String[] SUPPORTED_LANGUAGES = {"zh-CN", "en-US"};

    /** HTTP Accept-Language 请求头 */
    String LANGUAGE_HEADER = "Accept-Language";

    /** URL 参数切换语言 */
    String LANGUAGE_PARAM = "lang";

    /** Feign 调用传递语言的 Header */
    String FEIGN_LANGUAGE_HEADER = "X-Language";
}
