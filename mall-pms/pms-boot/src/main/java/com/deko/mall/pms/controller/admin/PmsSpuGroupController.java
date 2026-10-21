package com.deko.mall.pms.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.deko.mall.common.result.Result;
import com.deko.mall.pms.model.entity.PmsSpuGroup;
import com.deko.mall.pms.model.form.SpuGroupWithSpuForm;
import com.deko.mall.pms.model.query.PmsSpuGroupQuery;
import com.deko.mall.pms.model.vo.PmsSpuGroupRenderVO;
import com.deko.mall.pms.model.vo.PmsSpuVO;
import com.deko.mall.pms.service.PmsSpuGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分组管理接口
 */
@Tag(name = "商品分组管理")
@RestController
@RequestMapping("/pms-api/v1/spu-groups")
@RequiredArgsConstructor
public class PmsSpuGroupController {

    private final PmsSpuGroupService spuGroupService;

    @Operation(summary = "商品分组分页列表", description = "获取商品分组的分页列表，支持按名称、编码、状态等条件筛选")
    @GetMapping("/pages")
    public Result<IPage<PmsSpuGroupRenderVO>> listSpuGroupPages(PmsSpuGroupQuery queryParams) {
        IPage<PmsSpuGroupRenderVO> result = spuGroupService.listSpuGroupPages(queryParams);
        return Result.success(result);
    }

    @Operation(summary = "获取所有启用的分组", description = "获取所有启用的商品分组列表")
    @GetMapping("/enabled")
    public Result<List<PmsSpuGroupRenderVO>> listEnabledSpuGroups() {
        List<PmsSpuGroupRenderVO> result = spuGroupService.listEnabledSpuGroups();
        return Result.success(result);
    }

    @Operation(summary = "获取分组详情", description = "获取指定商品分组的详细信息")
    @GetMapping("/{id}")
    public Result<PmsSpuGroupRenderVO> getSpuGroupDetail(
            @Parameter(description = "商品分组ID", required = true) @PathVariable Long id) {
        PmsSpuGroupRenderVO result = spuGroupService.getSpuGroupDetail(id);
        return Result.success(result);
    }

    @Operation(summary = "创建商品分组", description = "创建新的商品分组")
    @PostMapping
    public Result<Long> createSpuGroup(@RequestBody PmsSpuGroup spuGroup) {
        boolean saved = spuGroupService.createSpuGroup(spuGroup);
        if (saved) {
            return Result.success(spuGroup.getId());
        }
        return Result.failed("创建失败");
    }

    @Operation(summary = "更新商品分组", description = "更新指定商品分组的信息")
    @PutMapping("/{id}")
    public Result<Boolean> updateSpuGroup(
            @Parameter(description = "商品分组ID", required = true) @PathVariable Long id,
            @RequestBody PmsSpuGroup spuGroup) {
        spuGroup.setId(id);
        boolean updated = spuGroupService.updateSpuGroup(spuGroup);
        return Result.judge(updated);
    }

    @Operation(summary = "删除商品分组", description = "删除指定的商品分组及其关联关系")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteSpuGroup(
            @Parameter(description = "商品分组ID", required = true) @PathVariable Long id) {
        boolean deleted = spuGroupService.deleteSpuGroup(id);
        return Result.judge(deleted);
    }

    @Operation(summary = "获取分组商品ID列表", description = "获取指定分组中的商品SPU ID列表")
    @GetMapping("/{id}/spus")
    public Result<List<Long>> getGroupSpus(
            @Parameter(description = "商品分组ID", required = true) @PathVariable Long id) {
        List<Long> productIds = spuGroupService.getGroupSpuIds(id);
        return Result.success(productIds);
    }

    @Operation(summary = "获取分组商品列表", description = "获取指定分组中的商品详细信息，包含商品名称、图片、价格等")
    @GetMapping("/{id}/spus/details")
    public Result<List<PmsSpuVO>> getGroupSpuDetails(
            @Parameter(description = "商品分组ID", required = true) @PathVariable Long id) {
        List<PmsSpuVO> groupSpuDetails = spuGroupService.getGroupSpuDetails(id);
        return Result.success(groupSpuDetails);
    }

    @Operation(summary = "添加商品到分组", description = "将指定商品添加到商品分组")
    @PostMapping("/{id}/spus")
    public Result<Boolean> addSpusToGroup(
            @Parameter(description = "商品分组ID", required = true) @PathVariable Long id,
            @RequestBody List<Long> spuIds) {
        boolean result = spuGroupService.addSpusToGroup(id, spuIds);
        return Result.judge(result);
    }

    @Operation(summary = "从分组移除商品", description = "从指定商品分组中移除商品")
    @DeleteMapping("/{id}/spus")
    public Result<Boolean> removeSpusFromGroup(
            @Parameter(description = "商品分组ID", required = true) @PathVariable Long id,
            @RequestBody List<Long> spuIds) {
        boolean result = spuGroupService.removeSpusFromGroup(id, spuIds);
        return Result.judge(result);
    }

    @Operation(summary = "创建商品分组(包含商品)", description = "创建新的商品分组，并同时添加商品 --聚合接口")
    @PostMapping("/with-spus")
    public Result<Boolean> createSpuGroupWithSpus(@RequestBody SpuGroupWithSpuForm form) {
        boolean result = spuGroupService.createSpuGroupWithSpus(form.getSpuGroup(), form.getSpuList());
        return Result.judge(result);
    }

    @Operation(summary = "更新商品分组(包含商品)", description = "更新商品分组信息并同时更新关联商品 -- 聚合接口")
    @PutMapping("/{id}/with-spus")
    public Result<Boolean> updateSpuGroupWithSpus(
            @Parameter(description = "商品分组ID", required = true) @PathVariable Long id,
            @RequestBody SpuGroupWithSpuForm form) {
        form.getSpuGroup().setId(id);
        boolean result = spuGroupService.updateSpuGroupWithSpus(form.getSpuGroup(), form.getSpuList());
        return Result.judge(result);
    }
}

