package com.deko.mall.pms.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import com.deko.mall.pms.model.form.ForbiddenAreaForm;
import com.deko.mall.pms.model.query.ForbiddenAreaQuery;
import com.deko.mall.pms.model.vo.ForbiddenAreaVO;
import com.deko.mall.pms.service.ForbiddenAreaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 禁售区域管理控制器
 */
@Tag(name = "管理端-禁售区域管理")
@RestController
@RequestMapping("/api/forbidden/area")
@RequiredArgsConstructor
public class PmsForbiddenAreaController {

    private final ForbiddenAreaService forbiddenAreaService;

    @Operation(summary = "分页查询禁售区域")
    @GetMapping("/page")
    public PageResult<ForbiddenAreaVO> pageForbiddenAreas(ForbiddenAreaQuery query) {
        IPage<ForbiddenAreaVO> page = forbiddenAreaService.pageForbiddenAreas(query);
        return PageResult.success(page);
    }

    @Operation(summary = "获取禁售区域列表")
    @GetMapping("/supplier/{supplierId}/list")
    public Result<List<ForbiddenAreaVO>> getForbiddenAreaList(
            @Parameter(description = "供应商ID") @PathVariable Long supplierId) {
        List<ForbiddenAreaVO> areas = forbiddenAreaService.getForbiddenAreaList(supplierId);
        return Result.success(areas);
    }

    @Operation(summary = "获取禁售区域详情")
    @GetMapping("/{id}")
    public Result<ForbiddenAreaVO> getForbiddenAreaDetail(
            @Parameter(description = "禁售区域ID") @PathVariable Long id) {
        ForbiddenAreaVO forbiddenArea = forbiddenAreaService.getForbiddenAreaDetail(id);
        return Result.success(forbiddenArea);
    }

    @Operation(summary = "创建禁售区域")
    @PostMapping
    public Result<Boolean> createForbiddenArea(@Valid @RequestBody ForbiddenAreaForm form) {
        boolean result = forbiddenAreaService.createForbiddenArea(form);
        return Result.judge(result);
    }

    @Operation(summary = "更新禁售区域")
    @PutMapping("/{id}")
    public Result<Boolean> updateForbiddenArea(
            @Parameter(description = "禁售区域ID") @PathVariable Long id,
            @Valid @RequestBody ForbiddenAreaForm form) {
        boolean result = forbiddenAreaService.updateForbiddenArea(id, form);
        return Result.judge(result);
    }

    @Operation(summary = "删除禁售区域")
    @DeleteMapping("/{ids}")
    public Result<Boolean> deleteForbiddenAreas(
            @Parameter(description = "禁售区域ID，多个以英文逗号(,)分割") @PathVariable String ids) {
        boolean result = forbiddenAreaService.deleteForbiddenAreas(
                Arrays.stream(ids.split(",")).map(Long::parseLong).toList());
        return Result.judge(result);
    }

    @Operation(summary = "检查区域是否在禁售区域内")
    @GetMapping("/check")
    public Result<Boolean> isInForbiddenArea(
            @Parameter(description = "区域编码") @RequestParam String regionCode,
            @Parameter(description = "供应商ID") @RequestParam Long supplierId) {
        boolean result = forbiddenAreaService.isInForbiddenArea(regionCode, supplierId);
        return Result.success(result);
    }
}
