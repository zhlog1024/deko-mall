package com.deko.mall.pms.controller.scm;

import com.deko.mall.common.result.Result;
import com.deko.mall.pms.model.entity.PmsSku;
import com.deko.mall.pms.service.SkuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Admin-商品SKU控制层
 */
@Tag(name = "Admin-商品SKU接口")
@RestController
@RequestMapping("/scm-api/v1/sku")
@RequiredArgsConstructor
public class ScmSkuController {
    private final SkuService skuService;

    @Operation(summary = "商品SKU详情")
    @GetMapping("/{skuId}")
    public Result getSkuDetail(
            @Parameter(name = "SkuId") @PathVariable Long skuId
    ) {
        PmsSku sku = skuService.getById(skuId);
        return Result.success(sku);
    }

    @Operation(summary = "修改SKU")
    @PutMapping(value = "/{skuId}")
    public Result updateSku(
            @Parameter(name = "SkuId") @PathVariable Long skuId,
            @RequestBody PmsSku sku
    ) {
        boolean result = skuService.updateById(sku);
        return Result.judge(result);
    }
}
