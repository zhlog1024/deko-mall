package com.deko.mall.pms.api;

import com.deko.mall.common.result.Result;
import com.deko.mall.common.web.config.FeignDecoderConfig;
import com.deko.mall.pms.model.dto.AtpChangeDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * SKU 可承诺量（ATP）内部写接口。
 *
 * 整单明细在 PMS 的同一本地事务中更新，避免逐 SKU 调用导致部分成功。
 * 查询使用 {@link SkuFeignClient#getSkuInfo(Long)}。
 */
@FeignClient(value = "mall-pms", contextId = "skuStock", configuration = {FeignDecoderConfig.class})
public interface SkuStockFeignClient {

    @Operation(summary = "占用 ATP（下单）", description = "校验 stock - locked_stock >= quantity；整批一个本地事务")
    @PutMapping("/feign-api/v1/sku-stock/lock")
    Result<Boolean> lock(@Parameter(description = "业务号（订单号/子单号）") @RequestParam("bizNo") String bizNo,
                         @RequestBody List<AtpChangeDTO> items);

    @Operation(summary = "释放 ATP 占用（取消/超时）", description = "整批一个本地事务；已释放则幂等")
    @PutMapping("/feign-api/v1/sku-stock/unlock")
    Result<Boolean> unlock(@RequestParam("bizNo") String bizNo,
                           @RequestBody List<AtpChangeDTO> items);

    @Operation(summary = "成交扣减（支付成功）", description = "实物与占用同时减 + 累加销量；整批一个本地事务")
    @PutMapping("/feign-api/v1/sku-stock/deduct")
    Result<Boolean> deduct(@RequestParam("bizNo") String bizNo,
                           @RequestBody List<AtpChangeDTO> items);

    @Operation(summary = "实物减少（不动占用）", description = "退款/回退路径内部使用")
    @PutMapping("/feign-api/v1/sku-stock/decrease")
    Result<Boolean> decreaseOnHand(@RequestParam("skuId") Long skuId,
                                  @RequestParam("quantity") Integer quantity);

    @Operation(summary = "实物回补（退款/回退）")
    @PutMapping("/feign-api/v1/sku-stock/restore")
    Result<Boolean> restoreOnHand(@RequestParam("skuId") Long skuId,
                                  @RequestParam("quantity") Integer quantity);
}
