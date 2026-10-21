package com.deko.mall.oms.service.order.factory;

import com.deko.mall.oms.model.form.OrderSubmitForm;
import com.deko.mall.oms.service.order.strategy.OrderProcessStrategy;
import com.deko.mall.pms.model.dto.SkuInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 订单策略工厂
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderStrategyFactory {

    private final List<OrderProcessStrategy> strategies;

    /**
     * 获取订单处理策略
     *
     * @param submitForm 订单提交表单
     * @param skuMap     商品SKU信息映射
     * @return 订单处理策略
     */
    public OrderProcessStrategy getStrategy(OrderSubmitForm submitForm, Map<Long, SkuInfoDTO> skuMap) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(submitForm, skuMap))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("没有找到支持的订单处理策略，订单类型：{}", submitForm.getOrderType());
                    return new IllegalArgumentException("没有找到支持的订单处理策略");
                });
    }
}
