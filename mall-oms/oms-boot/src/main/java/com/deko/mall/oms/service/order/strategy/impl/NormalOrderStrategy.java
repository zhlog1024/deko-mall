package com.deko.mall.oms.service.order.strategy.impl;

import com.deko.mall.common.security.util.SecurityUtils;
import com.deko.mall.oms.enums.OrderTypeEnum;
import com.deko.mall.oms.model.entity.order.OmsOrder;
import com.deko.mall.oms.model.form.OrderSubmitForm;
import com.deko.mall.oms.service.order.deduction.*;
import com.deko.mall.oms.service.order.strategy.OrderProcessStrategy;
import com.deko.mall.pms.model.dto.SkuInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * 普通订单处理策略
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(100)
public class NormalOrderStrategy implements OrderProcessStrategy {

    private final PointsStep pointsStep;

    @Override
    public boolean supports(OrderSubmitForm submitForm, Map<Long, SkuInfoDTO> skuMap) {
        if (OrderTypeEnum.NORMAL.getValue().equals(submitForm.getOrderType())) {
            return true;
        }
        if (submitForm.getOrderType() == null) {
            return true;
        }
        return false;
    }

    @Override
    public void preProcess(OrderSubmitForm submitForm, Map<Long, SkuInfoDTO> skuMap) {
        if (submitForm.getOrderType() == null) {
            submitForm.setOrderType(OrderTypeEnum.NORMAL.getValue());
        }
        log.info("普通订单处理 - 前置处理");
    }

    @Override
    public void validateOrder(OrderSubmitForm submitForm, Map<Long, SkuInfoDTO> skuMap) {
        log.info("普通订单处理 - 验证订单");
    }

    @Override
    public void calculateAmount(OmsOrder order, OrderSubmitForm submitForm, Map<Long, SkuInfoDTO> skuMap) {
        order.setOrderType(OrderTypeEnum.NORMAL.getValue());

        BigDecimal totalAmountBD = BigDecimal.ZERO;
        for (OrderSubmitForm.OrderItem item : submitForm.getOrderItems()) {
            SkuInfoDTO skuInfo = skuMap.get(item.getSkuId());
            item.setPrice(skuInfo.getRetailPrice());
            totalAmountBD = totalAmountBD.add(skuInfo.getRetailPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        BigDecimal freightAmount = order.getFreightAmount() != null ? order.getFreightAmount() : BigDecimal.ZERO;
        order.setTotalAmount(totalAmountBD.setScale(2, RoundingMode.CEILING));

        StepContext ctx = StepContext.builder()
                .memberId(SecurityUtils.getMemberId())
                .orderType(OrderTypeEnum.NORMAL)
                .skuMap(skuMap)
                .submitForm(submitForm)
                .order(order)
                .orderToken(submitForm.getOrderToken())
                .totalProductAmount(totalAmountBD)
                .freightAmount(freightAmount)
                .build();

        DeductionChain chain = DeductionChain.of(pointsStep);
        BigDecimal paymentAmount = chain.calculate(ctx);
        chain.applyToOrder(ctx);
        chain.lock(ctx);

        order.setPaymentAmount(paymentAmount.setScale(2, RoundingMode.CEILING));

        log.info("普通订单处理 - 计算金额, 商品总额: {}, 运费: {}, 积分抵扣: {}, 应付金额: {}",
                order.getTotalAmount(), freightAmount,
                order.getPointsDeductionAmount() != null ? order.getPointsDeductionAmount() : BigDecimal.ZERO,
                order.getPaymentAmount());
    }

    @Override
    public void postProcess(OmsOrder order, OrderSubmitForm submitForm, Map<Long, SkuInfoDTO> skuMap) {
        log.info("普通订单处理 - 后置处理");
    }
}
