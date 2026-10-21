package com.deko.mall.oms.service.order.deduction;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 单个抵扣 Step 的计算结果，自包含货币金额和资产数量。
 */
@Data
@AllArgsConstructor(staticName = "of")
public class StepResult {
    /** 商品金额抵扣（元） */
    private BigDecimal productDeduction;
    /** 运费抵扣（元） */
    private BigDecimal freightDeduction;
    /** 消耗的资产数量（个） */
    private BigDecimal assetUnits;

    public StepResult(BigDecimal productDeduction, BigDecimal freightDeduction) {
        this(productDeduction, freightDeduction, BigDecimal.ZERO);
    }

    public static final StepResult ZERO = of(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

    public StepResult add(StepResult other) {
        return of(
                this.productDeduction.add(other.productDeduction),
                this.freightDeduction.add(other.freightDeduction),
                this.assetUnits.add(other.assetUnits));
    }
}
