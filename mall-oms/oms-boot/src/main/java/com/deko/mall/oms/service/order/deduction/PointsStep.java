package com.deko.mall.oms.service.order.deduction;

import com.deko.mall.common.result.Result;
import com.deko.mall.oms.config.OrderConfig;
import com.deko.mall.oms.enums.OrderTypeEnum;
import com.deko.mall.pms.model.dto.SkuInfoDTO;
import com.deko.mall.ums.api.MemberAssetsOperateFeignClient;
import com.deko.mall.ums.dto.AssetOperateRequest;
import com.deko.mall.ums.dto.MemberAssetsDTO;
import com.deko.mall.ums.enums.AssetCodeEnum;
import com.deko.mall.ums.enums.AssetOperateSceneEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * 积分抵扣：可抵商品+运费，作为兜底抵扣。受 Nacos 开关控制。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PointsStep implements DeductionStep {

    private final OrderConfig orderConfig;
    private final MemberAssetsOperateFeignClient memberAssetsOperateFeignClient;

    @Override
    public int order() { return 2; }

    @Override
    public AssetCodeEnum assetCode() { return AssetCodeEnum.POINT; }

    @Override
    public boolean supports(OrderTypeEnum orderType, Map<Long, SkuInfoDTO> skuMap) {
        return true;
    }

    @Override
    public boolean canDeductProduct() { return true; }

    @Override
    public boolean canDeductFreight() { return true; }

    @Override
    public StepResult calculate(StepContext ctx, BigDecimal productPool, BigDecimal freightPool) {
        if (!isEnabled()) {
            return StepResult.ZERO;
        }

        BigDecimal available = fetchBalance(ctx.getMemberId());
        if (available.signum() <= 0) {
            return StepResult.ZERO;
        }

        // 优先抵商品，再抵运费
        BigDecimal productDeduction = available.min(productPool);
        BigDecimal remaining = available.subtract(productDeduction);
        BigDecimal freightDeduction = remaining.min(freightPool);
        BigDecimal totalDeduction = productDeduction.add(freightDeduction);

        return StepResult.of(productDeduction, freightDeduction, totalDeduction);
    }

    @Override
    public void applyToOrder(StepContext ctx, StepResult result) {
        BigDecimal total = result.getProductDeduction().add(result.getFreightDeduction());
        if (total.compareTo(BigDecimal.ZERO) <= 0) return;
        ctx.getOrder().setUsePointsAmount(total.setScale(2, RoundingMode.CEILING));
        ctx.getOrder().setPointsDeductionAmount(total.setScale(2, RoundingMode.CEILING));
    }

    @Override
    public void lock(StepContext ctx, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) return;
        AssetOperateRequest request = buildRequest(ctx, amount, "订单扣减积分");
        Result<Boolean> result = memberAssetsOperateFeignClient.operateAsset(request);
        if (!Result.isSuccess(result) || !result.getData()) {
            throw new com.deko.mall.common.web.exception.BizException("积分扣减失败");
        }
        log.info("订单[{}]扣减积分成功: memberId={}, amount={}",
                ctx.getOrderToken(), ctx.getMemberId(), amount);
    }

    private boolean isEnabled() {
        return orderConfig.getPointsDeductionEnabled() == null || orderConfig.getPointsDeductionEnabled();
    }

    private BigDecimal fetchBalance(Long memberId) {
        try {
            Result<MemberAssetsDTO> result = memberAssetsOperateFeignClient.getMemberAsset(AssetCodeEnum.POINT.getCode());
            if (Result.isSuccess(result) && result.getData() != null) {
                return result.getData().getAvailableNum() != null
                        ? result.getData().getAvailableNum() : BigDecimal.ZERO;
            }
        } catch (Exception e) {
            log.warn("获取积分异常，忽略余额抵扣: {}", e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    private AssetOperateRequest buildRequest(StepContext ctx, BigDecimal amount, String remark) {
        AssetOperateRequest req = new AssetOperateRequest();
        req.setMemberId(ctx.getMemberId());
        req.setAssetCode(assetCode().getCode());
        req.setNum(amount);
        req.setOperateType(AssetOperateSceneEnum.ORDER_CREATE_DEDUCT);
        req.setBizNo(ctx.getOrderToken());
        req.setRemark(remark);
        return req;
    }
}
