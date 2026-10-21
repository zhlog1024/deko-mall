package com.deko.mall.oms.service.order.deduction;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * 抵扣计算管道。
 * 持有有序的 DeductionStep 列表，按优先级依次执行。
 * 不关心每个 Step 的内部规则，只管传递剩余金额池和记录结果。
 */
@Slf4j
public class DeductionChain {

    private final List<DeductionStep> steps;

    public DeductionChain(List<DeductionStep> steps) {
        this.steps = steps.stream()
                .sorted(Comparator.comparingInt(DeductionStep::order))
                .toList();
    }

    public static DeductionChain of(DeductionStep... steps) {
        return new DeductionChain(Arrays.asList(steps));
    }

    /** 校验阶段：仅执行参与 VALIDATE 的 Step */
    public void validate(StepContext ctx) {
        for (DeductionStep step : steps) {
            if (step.supports(ctx.getOrderType(), ctx.getSkuMap())
                    && step.phases().contains(Phase.VALIDATE)) {
                step.validate(ctx);
            }
        }
    }

    /**
     * 计价阶段：依次执行各 Step（纯计算），传递剩余金额池，记录 stepResults。
     * @return 最终应付金额
     */
    public BigDecimal calculate(StepContext ctx) {
        BigDecimal productPool = ctx.getTotalProductAmount();
        BigDecimal freightPool = ctx.getFreightAmount();

        for (DeductionStep step : steps) {
            if (!step.supports(ctx.getOrderType(), ctx.getSkuMap())
                    || !step.phases().contains(Phase.CALCULATE)) {
                continue;
            }

            BigDecimal productForStep = step.canDeductProduct() ? productPool : BigDecimal.ZERO;
            BigDecimal freightForStep = step.canDeductFreight() ? freightPool : BigDecimal.ZERO;

            StepResult r = step.calculate(ctx, productForStep, freightForStep);
            ctx.recordLine(step.assetCode(), r);

            if (step.canDeductProduct()) {
                productPool = productPool.subtract(r.getProductDeduction());
            }
            if (step.canDeductFreight()) {
                freightPool = freightPool.subtract(r.getFreightDeduction());
            }

            log.debug("{} deducted product={}, freight={}, remaining product={}, freight={}",
                    step.getClass().getSimpleName(),
                    r.getProductDeduction(), r.getFreightDeduction(),
                    productPool, freightPool);
        }

        return productPool.add(freightPool).max(BigDecimal.ZERO);
    }

    /** 将链中各 Step 的计算结果写入订单实体（仅 submit 流程调用） */
    public void applyToOrder(StepContext ctx) {
        for (DeductionStep step : steps) {
            if (!step.supports(ctx.getOrderType(), ctx.getSkuMap())) continue;
            StepResult r = ctx.getLine(step.assetCode());
            if (r.getProductDeduction().signum() > 0 || r.getFreightDeduction().signum() > 0) {
                step.applyToOrder(ctx, r);
            }
        }
    }

    /** 锁定阶段：依次执行各 Step 的实际资产扣减 */
    public void lock(StepContext ctx) {
        for (DeductionStep step : steps) {
            if (step.supports(ctx.getOrderType(), ctx.getSkuMap())
                    && step.phases().contains(Phase.LOCK)) {
                step.lock(ctx, ctx.getLine(step.assetCode()).getAssetUnits());
            }
        }
    }
}
