package com.deko.mall.system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * 企业微信机器人配置属性
 */
@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "wecom")
public class WeComProperties {

    /**
     * 企业微信群机器人 Webhook URL
     */
    private String webhookUrl;

}
