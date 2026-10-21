package com.deko.mall.pay.config;

import lombok.Data;

/**
 * 支付配置
 */
@Data
public class PayProperties {

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 商户ID
     */
    private String mchId;

    /**
     * 商户私钥
     */
    private String privateKey;

    /**
     * 支付平台公钥
     */
    private String publicKey;


    /**
     * 支付异步回调地址
     */
    private String notifyUrl;

    /**
     * 支付同步回调地址
     */
    private String returnUrl;


}
