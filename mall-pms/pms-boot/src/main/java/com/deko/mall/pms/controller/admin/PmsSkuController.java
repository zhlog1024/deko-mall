package com.deko.mall.pms.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import com.deko.mall.pms.converter.SkuConverter;
import com.deko.mall.pms.model.entity.PmsSku;
import com.deko.mall.pms.model.form.PmsSkuForm;
import com.deko.mall.pms.model.query.SpuPageQuery;
import com.deko.mall.pms.model.vo.PmsSkuVO;
import com.deko.mall.pms.model.vo.SkuInfoVO;
import com.deko.mall.pms.service.SkuService;
import com.deko.mall.pms.service.SpuService;
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
@RequestMapping("/api/v1/sku")
@RequiredArgsConstructor
public class PmsSkuController {
    private final SkuService skuService;

    private final SkuConverter skuConverter;

    private final SpuService spuService;

    @Operation(summary = "商品SKU详情")
    @GetMapping("/{skuId}")
    public Result getSkuDetail(
            @Parameter(name = "SkuId") @PathVariable Long skuId
    ) {
        PmsSku sku = skuService.getById(skuId);
        PmsSkuVO pmsSkuVO = skuConverter.entity2VO(sku);
        return Result.success(pmsSkuVO);
    }

    @Operation(summary = "商品分页列表")
    @GetMapping("/page")
    public PageResult listPagedSku(SpuPageQuery queryParams) {
        IPage<SkuInfoVO> result = skuService.listPagedSku(queryParams);
        return PageResult.success(result);
    }

    @Operation(summary = "修改SKU")
    @PutMapping(value = "/{skuId}")
    public Result updateSku(
            @Parameter(name = "SkuId") @PathVariable Long skuId,
            @RequestBody PmsSkuForm sku
    ) {
        PmsSku pmsSku = skuConverter.form2Entity(sku);
        boolean result = skuService.updateById(pmsSku);
        return Result.judge(result);
    }

    @Operation(summary = "实时计算SKU零售毛利率")
    @PostMapping("/calculate-price")
    public Result<PmsSkuVO> calculatePrice(@RequestBody PmsSkuForm skuForm) {
        PmsSkuVO skuVO = new PmsSkuVO();

        // 如果有SKU ID，查询现有数据填充其他字段
        if (skuForm.getId() != null) {
            PmsSku existingSku = skuService.getById(skuForm.getId());
            if (existingSku != null) {
                skuVO = skuConverter.entity2VO(existingSku);
            }
        }

        // 使用请求中的价格覆盖
        if (skuForm.getRetailPrice() != null) {
            skuVO.setRetailPrice(skuForm.getRetailPrice());
        }
        if (skuForm.getSupplyPrice() != null) {
            skuVO.setSupplyPrice(skuForm.getSupplyPrice());
        }

        // 计算零售毛利率
        spuService.calculateSkuPrice(skuVO);

        return Result.success(skuVO);
    }

    @Operation(summary = "实时计算SKU零售毛利率")
    @PostMapping("/calculate-margin")
    public Result<PmsSkuVO> calculateMargin(@RequestBody PmsSkuForm skuForm) {
        PmsSkuVO skuVO = new PmsSkuVO();

        // 如果有SKU ID，查询现有数据填充其他字段
        if (skuForm.getId() != null) {
            PmsSku existingSku = skuService.getById(skuForm.getId());
            if (existingSku != null) {
                skuVO = skuConverter.entity2VO(existingSku);
            }
        }

        // 使用请求中的价格覆盖
        if (skuForm.getRetailPrice() != null) {
            skuVO.setRetailPrice(skuForm.getRetailPrice());
        }
        if (skuForm.getSupplyPrice() != null) {
            skuVO.setSupplyPrice(skuForm.getSupplyPrice());
        }

        spuService.calculateSkuMargin(skuVO);

        return Result.success(skuVO);
    }
}
