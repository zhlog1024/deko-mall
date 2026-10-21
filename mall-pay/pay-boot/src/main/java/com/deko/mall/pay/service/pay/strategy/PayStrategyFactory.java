package com.deko.mall.pay.service.pay.strategy;

import com.deko.mall.common.web.exception.BizException;
import com.deko.mall.pay.api.enums.PayChannelEnum;
import com.deko.mall.pay.config.PayResultCode;
import com.deko.mall.pay.entity.PayConfig;
import com.deko.mall.pay.service.pay.PayConfigProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支付策略工厂
 */
@Slf4j
@Component
public class PayStrategyFactory {

    private final Map<String, PayStrategy> strategyMap = new ConcurrentHashMap<>();
    private final PayConfigProvider payConfigProvider;

    @Autowired
    public PayStrategyFactory(List<PayStrategy> strategies, PayConfigProvider payConfigProvider) {
        this.payConfigProvider = payConfigProvider;
        strategies.forEach(strategy -> {
            String channel = strategy.getPayChannel();
            // 校验支付渠道是否合法
            PayChannelEnum channelEnum = null;
            for (PayChannelEnum value : PayChannelEnum.values()) {
                if (value.getValue().equals(channel)) {
                    channelEnum = value;
                    break;
                }
            }
            if (channelEnum == null) {
                log.error("Unsupported pay channel in strategy: {}", channel);
                return;
            }
            strategyMap.put(channel, strategy);
            log.info("支付渠道策略注册成功: {}", channelEnum.getValue());
        });
    }

    /**
     * 获取支付策略并初始化
     *
     * @param channel 支付渠道
     * @return 初始化后的支付策略
     */
    public PayStrategy getStrategy(String channel) {
        PayChannelEnum channelEnum = null;
        for (PayChannelEnum value : PayChannelEnum.values()) {
            if (value.getValue().equals(channel)) {
                channelEnum = value;
                break;
            }
        }
        if (channelEnum == null) {
            throw new BizException(PayResultCode.PAY_CHANNEL_NOT_SUPPORTED, channel);
        }

        PayStrategy strategy = strategyMap.get(channel);
        if (strategy == null) {
            throw new BizException(PayResultCode.CHANNEL_NOT_CONFIGURED, channelEnum.getLabel());
        }

        // 获取配置并初始化策略
        PayConfig config = payConfigProvider.getConfig(channel);
        if (config == null) {
            throw new BizException(PayResultCode.CHANNEL_CONFIG_NOT_FOUND, channelEnum.getLabel());
        }

        // 初始化策略
        try {
            strategy.initStrategy(config);
            log.debug("支付策略初始化成功: {}", channel);
        } catch (Exception e) {
            log.error("支付策略初始化失败: {} - {}", channel, e.getMessage());
            throw new BizException(PayResultCode.CHANNEL_INIT_FAILED, e.getMessage());
        }

        return strategy;
    }
}
