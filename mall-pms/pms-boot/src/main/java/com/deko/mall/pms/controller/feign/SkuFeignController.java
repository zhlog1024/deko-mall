package com.deko.mall.pms.controller.feign;

import com.deko.mall.common.result.Result;
import com.deko.mall.pms.api.SkuFeignClient;
import com.deko.mall.pms.model.dto.SkuInfoDTO;
import com.deko.mall.pms.service.SkuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SKU 内部库存查询接口。
 */
@Tag(name = "feign-商品库存接口", description = "提供商品SKU库存管理相关接口")
@RestController
@RequestMapping("/feign-api/v1/skus")
@RequiredArgsConstructor
public class SkuFeignController implements SkuFeignClient {

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

    @Operation(summary = "按关键字搜索 SKU ID", description = "名称/SKU 编码模糊匹配，供其它模块的列表页关键字筛选用")
    @GetMapping("/search-ids")
    public Result<List<Long>> searchSkuIds(
            @Parameter(description = "关键字") @RequestParam("keyword") String keyword,
            @Parameter(description = "最多返回条数") @RequestParam(value = "limit", required = false) Integer limit) {
        return Result.success(skuService.searchSkuIds(keyword, limit));
    }

}