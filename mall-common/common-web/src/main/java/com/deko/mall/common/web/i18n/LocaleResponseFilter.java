package com.deko.mall.common.web.i18n;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.IOException;
import java.util.Locale;

/**
 * 响应头注入 Content-Language
 * <p>
 * 根据当前请求的 {@link LocaleContextHolder} 设置 {@code Content-Language} 响应头，
 * 让前端和网关明确知道本次响应的语言。
 */
@Slf4j
public class LocaleResponseFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            Locale locale = LocaleContextHolder.getLocale();
            if (locale != null && response instanceof HttpServletResponse httpResponse) {
                httpResponse.setHeader("Content-Language", locale.toLanguageTag());
            }
        } catch (Exception ignored) {
        }
        chain.doFilter(request, response);
    }
}
