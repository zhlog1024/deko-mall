package com.deko.mall.pay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * 微信支付配置
 */
@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "pay.wxpay")
public class WxPayProperties {
    /**
     * 应用ID
     */
    private String appId;

    /**
     * 商户号
     */
    private String mchId;

    /**
     * 商户密钥
     */
    private String mchKey;

    /**
     * 商户证书路径
     */
    private String certPath;

    /**
     * API版本
     */
    private String apiVersion = "v2";

    /**
     * 微信jsapi支付结果通知地址
     */
    private String paymentNotifyUrl;

    /**
     * 微信jsapi支付结果通知地址
     */
    private String refundNotifyUrl;


}
