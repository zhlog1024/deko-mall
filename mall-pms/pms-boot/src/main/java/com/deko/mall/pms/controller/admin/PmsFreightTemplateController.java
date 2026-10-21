package com.deko.mall.pms.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import com.deko.mall.pms.model.form.FreightTemplateForm;
import com.deko.mall.pms.model.query.FreightTemplateQuery;
import com.deko.mall.pms.model.vo.FreightTemplateVO;
import com.deko.mall.pms.service.FreightTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 运费模板管理控制器
 */
@Tag(name = "管理端-运费模板管理")
@RestController
@RequestMapping("/api/freight/template")
@RequiredArgsConstructor
public class PmsFreightTemplateController {

    private final FreightTemplateService freightTemplateService;

    @Operation(summary = "分页查询运费模板")
    @GetMapping("/page")
    public PageResult<FreightTemplateVO> pageFreightTemplates(FreightTemplateQuery query) {
        IPage<FreightTemplateVO> page = freightTemplateService.pageFreightTemplates(query);
        return PageResult.success(page);
    }

    @Operation(summary = "获取运费模板详情")
    @GetMapping("/{id}")
    public Result<FreightTemplateVO> getFreightTemplateDetail(
            @Parameter(description = "运费模板ID") @PathVariable Long id) {
        FreightTemplateVO template = freightTemplateService.getFreightTemplateDetail(id);
        return Result.success(template);
    }

    @Operation(summary = "创建运费模板")
    @PostMapping
    public Result<Boolean> createFreightTemplate(@Valid @RequestBody FreightTemplateForm form) {
        boolean result = freightTemplateService.createFreightTemplate(form);
        return Result.judge(result);
    }

    @Operation(summary = "更新运费模板")
    @PutMapping("/{id}")
    public Result<Boolean> updateFreightTemplate(
            @Parameter(description = "运费模板ID") @PathVariable Long id,
            @Valid @RequestBody FreightTemplateForm form) {
        boolean result = freightTemplateService.updateFreightTemplate(id, form);
        return Result.judge(result);
    }

    @Operation(summary = "删除运费模板")
    @DeleteMapping("/{ids}")
    public Result<Boolean> deleteFreightTemplates(
            @Parameter(description = "运费模板ID，多个以英文逗号(,)分割") @PathVariable String ids) {
        boolean result = freightTemplateService.deleteFreightTemplates(
                Arrays.stream(ids.split(",")).map(Long::parseLong).toList());
        return Result.judge(result);
    }

    @Operation(summary = "获取运费模板列表")
    @GetMapping("/supplier/{supplierId}/list")
    public Result<List<FreightTemplateVO>> getFreightTemplateList(
            @Parameter(description = "供应商ID") @PathVariable Long supplierId) {
        List<FreightTemplateVO> templates = freightTemplateService.getFreightTemplateList(supplierId);
        return Result.success(templates);
    }

    @Operation(summary = "获取供应商默认运费模板")
    @GetMapping("/supplier/{supplierId}/default")
    public Result<FreightTemplateVO> getSupplierDefaultTemplate(
            @Parameter(description = "供应商ID") @PathVariable Long supplierId) {
        FreightTemplateVO template = freightTemplateService.getDefaultTemplate(supplierId);
        return Result.success(template);
    }


    @Operation(summary = "设置默认运费模板")
    @PutMapping("/{id}/default")
    public Result<Boolean> setDefaultTemplate(
            @Parameter(description = "运费模板ID") @PathVariable Long id) {
        boolean result = freightTemplateService.setDefaultTemplate(id);
        return Result.judge(result);
    }
}
