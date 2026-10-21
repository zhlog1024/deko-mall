package com.deko.mall.oms.service.logistics.strategy;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 物流策略工厂
 */
@Component
@RequiredArgsConstructor
public class LogisticsStrategyFactory {

    private final List<LogisticsStrategy> strategies;
    private final Map<String, LogisticsStrategy> strategyMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        strategies.forEach(strategy ->
                strategyMap.put(strategy.getProvider().getValue(), strategy)
        );
    }

    public LogisticsStrategy getStrategy(String provider) {
        return strategyMap.get(provider);
    }
}
