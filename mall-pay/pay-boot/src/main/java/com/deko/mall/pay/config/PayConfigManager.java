package com.deko.mall.pay.config;

import cn.hutool.json.JSONUtil;
import com.deko.mall.common.web.exception.BizException;
import com.deko.mall.pay.entity.AlipayExtConfig;
import com.deko.mall.pay.entity.PayConfig;
import com.deko.mall.pay.service.pay.PayConfigProvider;
import com.deko.mall.pay.service.pay.PayConfigService;
import com.deko.mall.pay.service.pay.strategy.PayStrategy;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.deko.mall.pay.config.PayResultCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 支付配置管理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayConfigManager implements PayConfigProvider {

    private final PayConfigService payConfigService;
    private final List<PayStrategy> strategies;

    /**
     * 支付渠道配置缓存，包含全部配置（启用和禁用）
     * key为支付渠道，value为该渠道的所有配置列表
     * enabled 由业务层自行过滤（如 getCashierInfo）
     */
    private final Map<String, List<PayConfig>> channelConfigsCache = new ConcurrentHashMap<>();

    /**
     * appId到配置的映射缓存，包含所有配置（启用和禁用）
     * 用于快速查找回调配置，即使配置被禁用也能处理之前发起的支付请求的回调
     * key为appId，value为对应的支付配置
     */
    private final Map<String, PayConfig> appIdConfigCache = new ConcurrentHashMap<>();

    /**
     * 渠道轮询计数器，用于实现轮询策略
     */
    private final Map<String, AtomicInteger> channelCounters = new ConcurrentHashMap<>();

    /**
     * 是否启用轮询策略，默认为true
     */
    @Getter
    @Value("${payment.polling-enabled:true}")
    private boolean pollingEnabled;

    @PostConstruct
    public void init() {
        initAllConfigs();
    }

    /**
     * 初始化所有支付配置
     */
    private void initAllConfigs() {
        // 获取所有配置（无论是否启用）
        List<PayConfig> allConfigs = payConfigService.getAllConfigs();
        log.info("开始初始化所有支付配置，总数：{}", allConfigs.size());

        // 清理缓存
        channelConfigsCache.clear();
        appIdConfigCache.clear();

        // 按支付渠道分组处理
        Map<String, List<PayConfig>> allChannelConfigs = new HashMap<>();

        for (PayConfig config : allConfigs) {
            String channel = config.getPayChannel();

            // 将配置按渠道分组
            allChannelConfigs.computeIfAbsent(channel, k -> new ArrayList<>()).add(config);

            // 初始化单个配置
            initSingleConfig(config);

            // 缓存appId到配置的映射
            if (config.getAppId() != null && !config.getAppId().isEmpty()) {
                appIdConfigCache.put(config.getAppId(), config);
                log.info("缓存应用ID[{}]到配置[{}]的映射", config.getAppId(), config.getId());
            }
        }

        // 更新缓存（全量，enabled 由业务层过滤）
        channelConfigsCache.putAll(allChannelConfigs);

        // 输出日志
        allChannelConfigs.forEach((channel, configs) -> {
            log.info("支付渠道[{}]配置数：{}", channel, configs.size());
        });
    }

    /**
     * 初始化单个支付配置
     */
    private void initSingleConfig(PayConfig payConfig) {
        try {
            if (payConfig == null) {
                log.warn("支付配置为null，跳过初始化");
                return;
            }

            // 查找对应支付渠道的策略
            PayStrategy strategy = findStrategy(payConfig.getPayChannel());
            if (strategy == null) {
                log.warn("找不到支付渠道[{}]对应的策略实现，跳过初始化", payConfig.getPayChannel());
                return;
            }

            // 初始化策略
            strategy.initStrategy(payConfig);
            log.info("初始化支付配置[{}]成功，渠道：{}", payConfig.getId(), payConfig.getPayChannel());
        } catch (Exception e) {
            log.error("初始化支付配置[{}]失败", payConfig.getId(), e);
        }
    }

    /**
     * 从策略列表中查找对应渠道的策略
     */
    private PayStrategy findStrategy(String channel) {
        if (channel == null || channel.isEmpty()) {
            return null;
        }

        for (PayStrategy strategy : strategies) {
            if (channel.equals(strategy.getPayChannel())) {
                return strategy;
            }
        }

        return null;
    }

    /**
     * 获取支付配置
     * 如果启用了轮询，则按轮询策略返回配置；否则返回第一个配置
     */
    @Override
    public PayConfig getConfig(String payChannel) {
        List<PayConfig> configs = channelConfigsCache.get(payChannel);
        if (configs == null || configs.isEmpty()) {
            log.error("找不到支付渠道[{}]的支付配置", payChannel);
            throw new BizException(PayResultCode.PAY_CONFIG_NOT_FOUND);
        }

        // 如果只有一个配置或者轮询未启用，直接返回第一个
        if (configs.size() == 1 || !pollingEnabled) {
            return configs.get(0);
        }

        // 使用轮询策略选择配置
        return getConfigByRoundRobin(payChannel, configs);
    }

    /**
     * 使用轮询策略选择配置
     */
    private PayConfig getConfigByRoundRobin(String payChannel, List<PayConfig> configs) {
        // 获取计数器，不存在则创建
        AtomicInteger counter = channelCounters.computeIfAbsent(payChannel, k -> new AtomicInteger(0));

        // 获取当前索引并递增
        int index = counter.getAndIncrement() % configs.size();

        // 重置计数器，防止溢出
        if (counter.get() > 10000) {
            counter.set(0);
        }

        PayConfig config = configs.get(index);
        log.info("支付渠道[{}]使用轮询策略选择了配置: {}", payChannel, config.getAppId());

        return config;
    }

    /**
     * 重新加载支付配置
     * 当配置变更时调用，会重新加载所有的支付配置
     */
    public void reloadConfig(String payChannel) {
        // 重新加载所有配置
        initAllConfigs();
        log.info("支付渠道[{}]配置已重新加载", payChannel);
    }

    /**
     * 设置是否启用轮询策略
     */
    public void setPollingEnabled(boolean pollingEnabled) {
        this.pollingEnabled = pollingEnabled;
        log.info("支付配置轮询策略已{}", pollingEnabled ? "启用" : "禁用");
    }

    /**
     * 根据appId获取支付配置，用于支付回调场景
     * 注意：此方法返回配置无论是否启用，用于回调验签
     *
     * @param appId 应用ID，比如支付宝的app_id、微信的mch_id等
     * @return 对应的支付配置，如果找不到则返回null
     */
    public PayConfig getConfigByAppId(String appId) {
        if (appId == null || appId.isEmpty()) {
            return null;
        }

        // 直接从 appId 缓存中获取
        PayConfig config = appIdConfigCache.get(appId);
        if (config != null) {
            log.debug("从缓存中找到appId[{}]对应的支付配置", appId);
            return config;
        }

        log.warn("根据appId[{}]找不到对应的支付配置", appId);
        return null;
    }


    /**
     * 从支付配置中获取支付宝的扩展配置
     *
     * @param config 支付配置
     * @return 支付宝的扩展配置，如果无法获取则返回null
     */
    public AlipayExtConfig getAlipayExtConfig(PayConfig config) {
        if (config == null || config.getExtConfig() == null || config.getExtConfig().isEmpty()) {
            return null;
        }

        try {
            // 直接解析JSON格式的扩展配置
            return JSONUtil.toBean(config.getExtConfig(), AlipayExtConfig.class);
        } catch (Exception e) {
            log.error("解析支付宝扩展配置失败", e);
            return null;
        }
    }
}
