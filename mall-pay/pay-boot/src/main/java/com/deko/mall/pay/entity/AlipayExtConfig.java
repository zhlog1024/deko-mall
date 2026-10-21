package com.deko.mall.pay.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 支付宝扩展配置
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AlipayExtConfig extends PayExtConfig {
    // 基础配置
    private String appPrivateKey;     // 应用私钥
    private String alipayPublicKey;   // 支付宝公钥(简单模式)
//
// 支付策略配置
    @JsonIgnore
    private String timeoutExpress = "30m";  // 超时时间表达式，默认30分钟
    // 支付宝网关配置
    @JsonIgnore
    private String serviceUrl = "https://openapi.alipay.com/gateway.do"; // 网关地址，默认为正式环境
    @JsonIgnore
    private String charset = "utf-8"; // 字符集
    @JsonIgnore
    private String signType = "RSA2"; // 签名类型
}
