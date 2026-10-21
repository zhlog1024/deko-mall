package com.deko.mall.oms.service.order.deduction;

import com.deko.mall.oms.enums.OrderTypeEnum;
import com.deko.mall.oms.model.entity.order.OmsOrder;
import com.deko.mall.oms.model.form.OrderSubmitForm;
import com.deko.mall.pms.model.dto.SkuInfoDTO;
import com.deko.mall.ums.enums.AssetCodeEnum;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

/**
 * 抵扣计算上下文，贯穿整个管道。
 * submitOrder 和 confirmOrder 共用，通过是否有 order 区分执行/预览模式。
 */
@Data
@Builder
public class StepContext {
    private Long memberId;
    private OrderTypeEnum orderType;
    private Map<Long, SkuInfoDTO> skuMap;
    private OrderSubmitForm submitForm;
    /** submit 时有值，confirm 预览时为 null */
    private OmsOrder order;
    private String orderToken;
    /** 商品总额（基础金额，不变） */
    private BigDecimal totalProductAmount;
    /** 运费（不变） */
    private BigDecimal freightAmount;

    /** 各资产抵扣明细：货币金额 + 资产数量，一步记录不遗漏 */
    @Builder.Default
    private Map<AssetCodeEnum, StepResult> lines = new EnumMap<>(AssetCodeEnum.class);

    public void recordLine(AssetCodeEnum code, StepResult result) {
        lines.merge(code, result, StepResult::add);
    }

    public StepResult getLine(AssetCodeEnum code) {
        return lines.getOrDefault(code, StepResult.ZERO);
    }

    /** 所有 Step 的商品抵扣总额 */
    public BigDecimal getTotalProductDeduction() {
        return lines.values().stream()
                .map(StepResult::getProductDeduction)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** 所有 Step 的运费抵扣总额 */
    public BigDecimal getTotalFreightDeduction() {
        return lines.values().stream()
                .map(StepResult::getFreightDeduction)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
