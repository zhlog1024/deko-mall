package com.deko.mall.ums.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * 会员缓存配置
 */
@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "mall.member.cache")
public class MemberCacheConfig {

    /**
     * 个人中心信息缓存时间（秒），默认3秒
     */
    private Long infoCacheSeconds = 3L;
}
