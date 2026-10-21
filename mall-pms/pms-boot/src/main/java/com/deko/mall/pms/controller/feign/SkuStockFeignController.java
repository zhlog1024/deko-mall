package com.deko.mall.pms.controller.feign;

import com.deko.mall.common.result.Result;
import com.deko.mall.pms.api.SkuStockFeignClient;
import com.deko.mall.pms.mapper.PmsSpuMapper;
import com.deko.mall.pms.model.dto.AtpChangeDTO;
import com.deko.mall.pms.support.SkuAtpSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * SKU 可承诺量写接口，统一调用 {@link SkuAtpSupport}。
 *
 * 条件更新防止余额越界；重复请求由调用方协调去重。
 */
@Tag(name = "Feign-SKU可承诺量(ATP)")
@Slf4j
@RestController
@RequiredArgsConstructor
public class SkuStockFeignController implements SkuStockFeignClient {

    private final SkuAtpSupport skuAtpSupport;

    private final PmsSpuMapper spuMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> lock(String bizNo, List<AtpChangeDTO> items) {
        apply(bizNo, items, "lock");
        return Result.success(true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> unlock(String bizNo, List<AtpChangeDTO> items) {
        apply(bizNo, items, "unlock");
        return Result.success(true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> deduct(String bizNo, List<AtpChangeDTO> items) {
        apply(bizNo, items, "deduct");
        // 销量累加（SPU 展示口径，与库存同属 SKU/SPU 派生计数，由 pms 独占写）
        if (items != null) {
            for (AtpChangeDTO item : items) {
                if (item.getSpuId() != null && item.getQuantity() != null && item.getQuantity() > 0) {
                    spuMapper.incrementSales(item.getSpuId(), item.getQuantity());
                }
            }
        }
        return Result.success(true);
    }

    @Override
    public Result<Boolean> decreaseOnHand(Long skuId, Integer quantity) {
        skuAtpSupport.decreaseOnHand(skuId, quantity == null ? 0 : quantity);
        return Result.success(true);
    }

    @Override
    public Result<Boolean> restoreOnHand(Long skuId, Integer quantity) {
        skuAtpSupport.restoreOnHand(skuId, quantity == null ? 0 : quantity);
        return Result.success(true);
    }

    private void apply(String bizNo, List<AtpChangeDTO> items, String op) {
        if (items == null || items.isEmpty()) {
            return;
        }
        for (AtpChangeDTO item : items) {
            int quantity = item.getQuantity() == null ? 0 : item.getQuantity();
            if (quantity <= 0) {
                continue;
            }
            switch (op) {
                case "lock" -> skuAtpSupport.lock(item.getSkuId(), quantity);
                case "unlock" -> skuAtpSupport.unlock(item.getSkuId(), quantity);
                case "deduct" -> skuAtpSupport.deduct(item.getSkuId(), quantity);
                default -> throw new IllegalStateException("不支持的 ATP 操作：" + op);
            }
        }
        log.info("ATP {}完成：bizNo={}, 行数={}", op, bizNo, items.size());
    }
}
