package com.deko.mall.system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * 维信短信配置
 */
@Configuration
@ConfigurationProperties(prefix = "sms.veesing")
@RefreshScope
@Data
public class VeesingSmsProperties {

    private String account;

    private String password;

    private String url;

    /**
     * 模板编码
     */
    private Map<String, String> templateCodes;
}
