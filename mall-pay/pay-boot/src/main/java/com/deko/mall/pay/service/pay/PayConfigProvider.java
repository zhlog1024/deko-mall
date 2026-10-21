package com.deko.mall.pay.service.pay;

import com.deko.mall.pay.entity.PayConfig;

/**
 * 支付配置提供者接口
 * 用于解决循环依赖问题，将PayConfigManager的配置获取功能抽象为接口
 */
public interface PayConfigProvider {

    /**
     * 根据支付渠道获取配置
     *
     * @param payChannel 支付渠道
     * @return 支付配置
     */
    PayConfig getConfig(String payChannel);

    /**
     * 根据appId获取配置
     *
     * @param appId 应用ID
     * @return 支付配置
     */
    PayConfig getConfigByAppId(String appId);
}
