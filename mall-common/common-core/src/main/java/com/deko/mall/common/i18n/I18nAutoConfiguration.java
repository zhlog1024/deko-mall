package com.deko.mall.common.i18n;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * 国际化自动配置
 */
@Slf4j
@AutoConfiguration
@Import(I18nUtils.class)
public class I18nAutoConfiguration {

    public I18nAutoConfiguration() {
        log.info("国际化模块已启用");
    }
}
