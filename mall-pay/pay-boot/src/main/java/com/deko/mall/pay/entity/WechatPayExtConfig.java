package com.deko.mall.pay.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/**
 * 微信支付扩展配置
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class WechatPayExtConfig extends PayExtConfig {
    // 基础配置
    private String apiV3Key;          // API V3密钥
    private String merchantPrivateKey; // 商户私钥(PEM格式)
    private String merchantSerialNo;   // 商户证书序列号
    private String platformCert;       // 平台证书
    private String platformCertSerialNo; // 平台证书序列号


    @JsonIgnore
    private String domain = "https://api.mch.weixin.qq.com"; // 微信支付域名，默认为中国地区域名
    @JsonIgnore
    private long timeoutExpress = 1000 * 60 * 3;  // 超时时间表达式，默认30分钟

    // 解析后的证书对象
    @JsonIgnore
    @TableField(exist = false)
    private transient PrivateKey parsedMerchantPrivateKey;

    @JsonIgnore
    @TableField(exist = false)
    private transient X509Certificate parsedPlatformCert;
}
