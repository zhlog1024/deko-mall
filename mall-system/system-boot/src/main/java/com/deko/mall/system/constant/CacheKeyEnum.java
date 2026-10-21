package com.deko.mall.system.constant;

import lombok.Getter;

/**
 * Redis 缓存配置枚举
 */
@Getter
public enum CacheKeyEnum {

    /** APP 全局配置缓存 —— 24小时 */
    APP_CONFIG("system:app:config", 24 * 60 * 60L);

    private final String key;
    private final long ttlSeconds;

    CacheKeyEnum(String key, long ttlSeconds) {
        this.key = key;
        this.ttlSeconds = ttlSeconds;
    }
}
