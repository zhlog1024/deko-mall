package com.deko.mall.oms.service.order.deduction;

import com.deko.mall.oms.enums.OrderTypeEnum;
import com.deko.mall.pms.model.dto.SkuInfoDTO;
import com.deko.mall.ums.enums.AssetCodeEnum;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Map;

/**
 * 抵扣计算步骤。
 * 每个实现代表一种资产抵扣类型（金豆/银豆/积分），自包含判断逻辑和计算公式。
 */
public interface DeductionStep {

    /** 执行优先级，越小越先 */
    int order();

    /** 操作的资产类型 */
    AssetCodeEnum assetCode();

    /** 是否适用于该订单类型 */
    boolean supports(OrderTypeEnum orderType, Map<Long, SkuInfoDTO> skuMap);

    /** 参与哪些阶段 */
    default EnumSet<Phase> phases() {
        return EnumSet.of(Phase.CALCULATE, Phase.LOCK);
    }

    /** 能否抵扣商品金额 */
    default boolean canDeductProduct() {
        return false;
    }

    /** 能否抵扣运费 */
    default boolean canDeductFreight() {
        return false;
    }

    /**
     * 校验阶段：余额检查 + 可选提前扣减。
     * 例如金豆在此时直接扣减，因为余额不足则订单不应创建。
     */
    default void validate(StepContext ctx) {}

    /**
     * 计价阶段：从给定的金额池中计算可抵扣额。纯计算，不修改任何外部状态。
     * @param ctx          上下文
     * @param productPool  当前剩余可抵的商品金额
     * @param freightPool  当前剩余可抵的运费
     */
    StepResult calculate(StepContext ctx, BigDecimal productPool, BigDecimal freightPool);

    /**
     * 将计算结果写到订单实体（仅 submit 流程调用，confirm 预览时跳过）。
     */
    default void applyToOrder(StepContext ctx, StepResult result) {}

    /**
     * 锁定阶段：实际调用资产接口扣减。
     * @param ctx    上下文
     * @param amount 要扣减的资产数量
     */
    default void lock(StepContext ctx, BigDecimal amount) {}
}
