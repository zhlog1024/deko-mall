package com.deko.mall.pms.controller.app;

import com.deko.mall.common.result.Result;
import com.deko.mall.pms.model.dto.SkuInfoDTO;
import com.deko.mall.pms.service.SkuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端 SKU 库存查询接口。
 */
@Tag(name = "App-商品库存接口", description = "提供商品SKU库存管理相关接口")
@RestController
@RequestMapping("/app-api/v1/skus")
@RequiredArgsConstructor
public class SkuController {

    private final SkuService skuService;

    @Operation(summary = "获取商品库存信息", description = "获取指定SKU的库存详细信息")
    @GetMapping("/{skuId}")
    public Result<SkuInfoDTO> getSkuInfo(
            @Parameter(description = "SKU ID") @PathVariable Long skuId) {
        SkuInfoDTO skuInfo = skuService.getSkuInfo(skuId);
        return Result.success(skuInfo);
    }

    @Operation(summary = "获取商品库存列表", description = "批量获取多个SKU的库存信息")
    @GetMapping
    public Result<List<SkuInfoDTO>> getSkuInfoList(
            @Parameter(description = "SKU ID列表") @RequestParam List<Long> skuIds) {
        List<SkuInfoDTO> skuInfos = skuService.listSkuInfos(skuIds);
        return Result.success(skuInfos);
    }

}