package com.deko.mall.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.deko.mall.system.constant.CacheKeyEnum;
import com.deko.mall.system.mapper.SysAppConfigMapper;
import com.deko.mall.system.model.entity.SysAppConfig;
import com.deko.mall.system.service.AppConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AppConfigServiceImpl implements AppConfigService {

    private final SysAppConfigMapper configMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final CacheKeyEnum CACHE_KEY = CacheKeyEnum.APP_CONFIG;
    private static final String APP_CONFIG_CACHE_KEY = CACHE_KEY.getKey();
    private static final long CONFIG_CACHE_EXPIRE_SECONDS = CACHE_KEY.getTtlSeconds();

    public AppConfigServiceImpl(SysAppConfigMapper configMapper,
            RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper) {
        this.configMapper = configMapper;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getAppConfig() {
        Map<String, Object> cached = (Map<String, Object>) redisTemplate.opsForValue().get(APP_CONFIG_CACHE_KEY);
        if (cached != null) {
            return cached;
        }
        Map<String, Object> config = buildConfig();
        redisTemplate.opsForValue().set(APP_CONFIG_CACHE_KEY, config,
                CONFIG_CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        return config;
    }

    private Map<String, Object> buildConfig() {
        // 1. 从数据库读取所有启用配置，key → value
        List<SysAppConfig> dbConfigs = configMapper.selectList(
                new LambdaQueryWrapper<SysAppConfig>()
                        .eq(SysAppConfig::getStatus, 1));

        Map<String, Object> config = new LinkedHashMap<>();
        config.put("status", 1);

        for (SysAppConfig c : dbConfigs) {
            // App UI 静态仅走包内，不下发远程静态根
            if ("common_static_url".equals(c.getConfigKey())) {
                continue;
            }
            Object value = autoParse(c.getConfigValue());
            // 跳过空字符串值（PHP：不存在的 key 不返回，前端 get_config 走 fallback）
            if (value instanceof String str && str.isEmpty()) {
                continue;
            }
            config.put(c.getConfigKey(), value);
        }

        // 2. 动态计算的 key（基于 DB 中的配置值拼装）
        // common_app_h5_url ← 已在 DB 中，由步骤 1 自然写入，无需额外处理
        // 协议 URL ← 从 DB 中 agreement_base_url 拼装
        Object baseObj = config.get("agreement_base_url");
        String baseUrl = baseObj instanceof String ? (String) baseObj : "";
        config.put("agreement_userregister_url", buildAgreementUrl(baseUrl, "userregister"));
        config.put("agreement_userprivacy_url", buildAgreementUrl(baseUrl, "userprivacy"));
        config.put("agreement_userlogout_url", buildAgreementUrl(baseUrl, "userlogout"));

        return config;
    }

    private String buildAgreementUrl(String baseUrl, String document) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            return "";
        }
        String base = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
        // 协议 URL 指向内容服务的 HTML 接口。
        return base + "/mall-cms/app-api/v1/agreement/" + document + "/html";
    }

    /** 自动解析 JSON：值以 { 或 [ 开头则尝试 parse，否则保持原字符串 */
    private Object autoParse(String v) {
        if (v == null || (v = v.trim()).isEmpty()) {
            return "";
        }
        char first = v.charAt(0);
        if (first == '{' || first == '[') {
            try {
                return objectMapper.readValue(v, Object.class);
            } catch (Exception ignored) {
            }
        }
        return v;
    }
}
