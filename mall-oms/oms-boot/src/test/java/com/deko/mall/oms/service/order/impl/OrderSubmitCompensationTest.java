package com.deko.mall.oms.service.order.impl;

import com.deko.mall.common.result.Result;
import com.deko.mall.common.web.exception.BizException;
import com.deko.mall.oms.enums.OrderSourceEnum;
import com.deko.mall.oms.model.entity.order.OmsOrder;
import com.deko.mall.oms.model.form.OrderSubmitForm;
import com.deko.mall.oms.service.order.factory.OrderStrategyFactory;
import com.deko.mall.pms.api.SkuFeignClient;
import com.deko.mall.pms.api.SkuStockFeignClient;
import com.deko.mall.pms.model.dto.SkuInfoDTO;
import com.deko.mall.ums.api.MemberAssetsOperateFeignClient;
import com.deko.mall.ums.dto.AssetOperateRequest;
import com.deko.mall.ums.enums.AssetCodeEnum;
import com.deko.mall.ums.enums.AssetOperateSceneEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 订单提交失败的补偿测试：库存锁定成功后，后续失败必须释放已占用量。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderSubmitCompensationTest {

    private static final String ORDER_TOKEN = "TEST_TOKEN_0001";
    private static final Long SKU_ID = 1771L;

    @Mock
    private SkuFeignClient skuFeignClient;

    @Mock
    private SkuStockFeignClient skuStockFeignClient;

    @Mock
    private OrderStrategyFactory orderStrategyFactory;

    @Mock
    private MemberAssetsOperateFeignClient memberAssetsOperateFeignClient;

    @Mock
    private StringRedisTemplate redisTemplate;

    @InjectMocks
    private OrderFacadeServiceImpl orderFacadeService;

    private SkuInfoDTO buildSku() {
        SkuInfoDTO sku = new SkuInfoDTO();
        sku.setId(SKU_ID);
        sku.setSpuId(SKU_ID);
        sku.setSpuType("NORMAL");
        sku.setSkuName("山楂茯苓颗粒200g（10g*20条） 默认");
        sku.setRetailPrice(new BigDecimal("13.80"));
        sku.setSupplyPrice(new BigDecimal("9.30"));
        return sku;
    }

    private OrderSubmitForm buildForm() {
        OrderSubmitForm form = new OrderSubmitForm();
        form.setOrderToken(ORDER_TOKEN);
        form.setOrderSource(OrderSourceEnum.APP);
        form.setShippingAddress(new OrderSubmitForm.ShippingAddress());
        List<OrderSubmitForm.OrderItem> items = new ArrayList<>();
        OrderSubmitForm.OrderItem item = new OrderSubmitForm.OrderItem();
        item.setSkuId(SKU_ID);
        item.setQuantity(1);
        items.add(item);
        form.setOrderItems(items);
        return form;
    }

    @Test
    @DisplayName("回归：锁定成功之后失败，必须释放已锁库存（不能用尚未填充的 orderNos）")
    void submitOrder_failureAfterLock_releasesLockedStock() {
        // 防重复提交校验通过（Redis 抢到 token）
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any())).thenReturn(1L);
        when(skuFeignClient.getSkuInfoList(anyList())).thenReturn(Result.success(List.of(buildSku())));
        when(skuStockFeignClient.lock(eq(ORDER_TOKEN), anyList())).thenReturn(Result.success(true));
        // 失败点选在「取策略」：位于锁库存（857）之后、orderNos.add（925）之前，正是本次泄漏的窗口
        when(orderStrategyFactory.getStrategy(any(), any()))
                .thenThrow(new BizException("模拟锁定后失败"));

        assertThrows(BizException.class, () -> orderFacadeService.submitOrder(buildForm()));

        // 零售只占 ATP：锁定与释放都走 pms 的 ATP 契约（不再经 store、不再动云仓实物账）
        verify(skuStockFeignClient).lock(eq(ORDER_TOKEN), anyList());
        verify(skuStockFeignClient).unlock(eq(ORDER_TOKEN), anyList());
    }



    private AssetOperateRequest findAsset(List<AssetOperateRequest> requests, AssetCodeEnum assetCode) {
        return requests.stream()
                .filter(r -> assetCode.getCode().equals(r.getAssetCode()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("未发起 " + assetCode.getCode() + " 的退款请求：" + requests));
    }
}
