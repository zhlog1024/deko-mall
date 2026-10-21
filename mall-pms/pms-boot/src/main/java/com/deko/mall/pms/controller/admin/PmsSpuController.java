package com.deko.mall.pms.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import com.deko.mall.common.security.util.SecurityUtils;
import com.deko.mall.pms.model.form.PmsSpuForm;
import com.deko.mall.pms.model.form.SpuAuditForm;
import com.deko.mall.pms.model.form.SpuPricingCalculateForm;
import com.deko.mall.pms.model.query.SpuPageQuery;
import com.deko.mall.pms.model.vo.PmsSpuDetailVO;
import com.deko.mall.pms.model.vo.PmsSpuPageVO;
import com.deko.mall.pms.model.vo.SpuCalculateResultVO;
import com.deko.mall.pms.service.SpuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Admin-商品控制层
 */
@Tag(name = "Admin-商品SPU接口")
@RestController
@RequestMapping("/api/v1/spu")
@AllArgsConstructor
@Slf4j
public class PmsSpuController {

    private SpuService spuService;

    @Operation(summary = "商品分页列表")
    @GetMapping("/page")
    public PageResult listPagedSpu(SpuPageQuery queryParams) {
        IPage<PmsSpuPageVO> result = spuService.listPagedSpu(queryParams);
        return PageResult.success(result);
    }


    @Operation(summary = "商品详情")
    @GetMapping("/{id}/detail")
    public Result detail(@Parameter(name = "商品ID") @PathVariable Long id) {
        PmsSpuDetailVO pmsSpuDetailVO = spuService.getSpuDetail(id);
        return Result.success(pmsSpuDetailVO);
    }

    @Operation(summary = "新增商品")
    @PostMapping
    public Result addSpu(@RequestBody PmsSpuForm formData) {
        boolean result = spuService.addSpu(formData);
        return Result.judge(result);
    }

    @Operation(summary = "修改商品")
    @PutMapping(value = "/{id}")
    public Result updateSpuById(
            @Parameter(name = "商品ID") @PathVariable Long id,
            @RequestBody PmsSpuForm formData
    ) {
        boolean result = spuService.updateSpuById(id, formData);
        return Result.judge(result);
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/{ids}")
    public Result delete(
            @Parameter(name = "商品ID,多个以英文逗号(,)分隔") @PathVariable String ids
    ) {
        boolean result = spuService.removeBySpuIds(ids);
        return Result.judge(result);
    }


    @Operation(summary = "审核商品")
    @PutMapping("/audit/{id}")
    public Result<Boolean> auditSpu(
            @PathVariable Long id,
            @RequestBody SpuAuditForm form
    ) {
        // 设置审核人信息
        form.setAuditUser(SecurityUtils.getUsername());
        form.setAuditTime(LocalDateTime.now());
        boolean result = spuService.auditSpu(id, form);
        return Result.judge(result);
    }

    @Operation(summary = "商品上架")
    @PutMapping("/{id}/publish")
    public Result<Boolean> publishSpu(@Parameter(name = "商品ID") @PathVariable Long id) {
        boolean result = spuService.publishSpu(id);
        return Result.judge(result);
    }

    @Operation(summary = "商品下架")
    @PutMapping("/{id}/unpublish")
    public Result<Boolean> unpublishSpu(@Parameter(name = "商品ID") @PathVariable Long id) {
        boolean result = spuService.unpublishSpu(id);
        return Result.judge(result);
    }

    @Operation(summary = "批量上架商品")
    @PutMapping("/batch/publish")
    public Result<Boolean> batchPublishSpu(@RequestBody List<Long> ids) {
        boolean result = spuService.batchPublishSpu(ids);
        return Result.judge(result);
    }

    @Operation(summary = "批量下架商品")
    @PutMapping("/batch/unpublish")
    public Result<Boolean> batchUnpublishSpu(@RequestBody List<Long> ids) {
        boolean result = spuService.batchUnpublishSpu(ids);
        return Result.judge(result);
    }

    @Operation(summary = "计算SPU定价信息（价格+毛利率+商品类型）")
    @PostMapping("/calculate-pricing")
    public Result<SpuCalculateResultVO> calculateSpuPricing(
            @RequestBody SpuPricingCalculateForm form) {
        
        // 将Form转换为VO
        List<com.deko.mall.pms.model.vo.PmsSkuVO> skuVOList = form.getSkuList().stream()
                .map(skuForm -> {
                    com.deko.mall.pms.model.vo.PmsSkuVO skuVO = new com.deko.mall.pms.model.vo.PmsSkuVO();
                    skuVO.setId(skuForm.getId());
                    skuVO.setRetailPrice(skuForm.getRetailPrice());
                    skuVO.setSupplyPrice(skuForm.getSupplyPrice());
                    return skuVO;
                })
                .collect(java.util.stream.Collectors.toList());
        
        SpuCalculateResultVO result = spuService.calculateSpuPricingInfo(
                form.getSpuId(),
                skuVOList,
                form.getSpuType()
        );
        
        return Result.success(result);
    }

    @Operation(summary = "根据ID批量获取SPU简要信息")
    @PostMapping("/batch-info")
    public Result<List<com.deko.mall.pms.model.vo.PmsSpuVO>> batchGetSpuInfo(@RequestBody List<Long> ids) {
        List<com.deko.mall.pms.model.dto.PmsSpuDTO> dtos = spuService.getProductList(ids);
        List<com.deko.mall.pms.model.vo.PmsSpuVO> result = dtos.stream().map(dto -> {
            com.deko.mall.pms.model.vo.PmsSpuVO vo = new com.deko.mall.pms.model.vo.PmsSpuVO();
            vo.setId(dto.getId());
            vo.setName(dto.getName());
            vo.setPicUrl(dto.getPicUrl());
            vo.setPrice(dto.getPrice());
            vo.setSpuTypeDesc(dto.getSpuTypeDesc());
            return vo;
        }).toList();
        return Result.success(result);
    }

}
