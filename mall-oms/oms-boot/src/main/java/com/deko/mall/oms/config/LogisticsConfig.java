package com.deko.mall.oms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * 物流配置类
 */
@Data
@Configuration
@RefreshScope
@ConfigurationProperties(prefix = "logistics")
public class LogisticsConfig {

    /**
     * 快递100配置
     */
    private Kuaidi100Config kuaidi100 = new Kuaidi100Config();

    @Data
    public static class Kuaidi100Config {
        /**
         * 授权key
         */
        private String key;

        /**
         * 用户编号
         */
        private String customer;

        /**
         * 回调地址
         */
        private String callbackUrl;

        /**
         * 签名盐值（快递100后台的 secret/回调签名密钥）
         */
        private String salt;

        /**
         * 企业用户ID
         */
        private String userid;
    }
}
