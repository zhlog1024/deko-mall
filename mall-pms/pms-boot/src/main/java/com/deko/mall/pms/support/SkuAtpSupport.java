package com.deko.mall.pms.support;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.deko.mall.pms.mapper.PmsSkuMapper;
import com.deko.mall.pms.model.entity.PmsSku;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 统一维护 SKU 可承诺量：stock 为供应商可供货量，locked_stock 为已占用量。
 *
 * 所有写入使用带余额条件的原子更新，防止超卖或库存为负；由调用方事务协调业务操作。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SkuAtpSupport {

    private final PmsSkuMapper pmsSkuMapper;

    /**
     * 占用 ATP：{@code locked_stock += quantity}
     * <p>守卫：可用量 {@code stock - locked_stock >= quantity}，不足则抛「商品库存不足」。
     *
     * @param skuId    SKU ID
     * @param quantity 数量（&le;0 视为无操作）
     */
    public void lock(Long skuId, int quantity) {
        if (quantity <= 0) {
            return;
        }
        boolean updated = pmsSkuMapper.update(null, new LambdaUpdateWrapper<PmsSku>()
                .setSql("locked_stock = locked_stock + " + quantity)
                .eq(PmsSku::getId, skuId)
                .apply("stock - locked_stock >= {0}", quantity)) > 0;
        Assert.isTrue(updated, "商品库存不足");
    }

    /**
     * 释放 ATP 占用：{@code locked_stock -= quantity}
     *
     * @param skuId    SKU ID
     * @param quantity 数量（&le;0 视为无操作）
     */
    public void unlock(Long skuId, int quantity) {
        if (quantity <= 0) {
            return;
        }
        boolean updated = pmsSkuMapper.update(null, new LambdaUpdateWrapper<PmsSku>()
                .setSql("locked_stock = locked_stock - " + quantity)
                .eq(PmsSku::getId, skuId)
                .ge(PmsSku::getLockedStock, quantity)) > 0;
        Assert.isTrue(updated, "解锁商品库存失败");
    }

    /**
     * 实物减少（ATP 口径的"已发/已提"）：{@code stock -= quantity}
     *
     * @param skuId    SKU ID
     * @param quantity 数量（&le;0 视为无操作）
     */
    public void decreaseOnHand(Long skuId, int quantity) {
        if (quantity <= 0) {
            return;
        }
        boolean updated = pmsSkuMapper.update(null, new LambdaUpdateWrapper<PmsSku>()
                .setSql("stock = stock - " + quantity)
                .eq(PmsSku::getId, skuId)
                .ge(PmsSku::getStock, quantity)) > 0;
        Assert.isTrue(updated, "扣减商品库存失败");
    }

    /**
     * 成交扣减（{@code stock -= n} 且 {@code locked_stock -= n}）：C 端支付成功、门店进货货权转移共用。
     *
     * @param skuId    SKU ID
     * @param quantity 数量（&le;0 视为无操作）
     */
    public void deduct(Long skuId, int quantity) {
        if (quantity <= 0) {
            return;
        }
        decreaseOnHand(skuId, quantity);
        unlock(skuId, quantity);
    }

    /**
     * 实物回补：{@code stock += quantity}（退款/退货把可承诺量退回时用）
     * <p>不涉及锁定列：退款时锁定早已在扣减环节释放。
     *
     * @param skuId    SKU ID
     * @param quantity 数量（&le;0 视为无操作）
     */
    public void restoreOnHand(Long skuId, int quantity) {
        if (quantity <= 0) {
            return;
        }
        boolean updated = pmsSkuMapper.update(null, new LambdaUpdateWrapper<PmsSku>()
                .setSql("stock = stock + " + quantity)
                .eq(PmsSku::getId, skuId)) > 0;
        Assert.isTrue(updated, "回补商品库存失败");
    }
}
