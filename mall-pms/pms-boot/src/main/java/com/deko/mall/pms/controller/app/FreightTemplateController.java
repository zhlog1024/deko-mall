package com.deko.mall.pms.controller.app;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import com.deko.mall.pms.model.dto.FreightTemplateDTO;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 供应商运费模板控制器
 */
@Tag(name = "供应商-运费模板管理")
@RestController
@RequestMapping("/app-api/freight/template")
@RequiredArgsConstructor
public class FreightTemplateController {

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

    @Operation(summary = "设置默认运费模板")
    @PutMapping("/{id}/default")
    public Result<Boolean> setDefaultTemplate(
            @Parameter(description = "运费模板ID") @PathVariable Long id) {
        boolean result = freightTemplateService.setDefaultTemplate(id);
        return Result.judge(result);
    }

    @Operation(summary = "获取默认运费模板")
    @GetMapping("/default")
    public Result<FreightTemplateVO> getDefaultTemplate() {
        FreightTemplateVO template = freightTemplateService.getDefaultTemplate(null);
        return Result.success(template);
    }

    /**
     * 根据ID集合批量获取运费模板
     *
     * @param ids 运费模板ID集合
     * @return 运费模板列表
     */
    @Operation(summary = "批量获取运费模板信息")
    @GetMapping("/v1/freight-templates/batch")
    public Result<List<FreightTemplateDTO>> getFreightTemplatesByIds(@RequestParam("ids") List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.success(new ArrayList<>());
        }



        return null;
    }

}
