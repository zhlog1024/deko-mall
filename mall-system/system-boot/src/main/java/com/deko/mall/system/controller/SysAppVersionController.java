package com.deko.mall.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import com.deko.mall.common.web.annotation.PreventDuplicateResubmit;
import com.deko.mall.system.model.form.AppVersionForm;
import com.deko.mall.system.model.query.AppVersionPageQuery;
import com.deko.mall.system.model.vo.AppVersionVO;
import com.deko.mall.system.service.SysAppVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "08.APP版本管理")
@RestController
@RequestMapping("/api/v1/app-version")
@RequiredArgsConstructor
public class SysAppVersionController {

    private final SysAppVersionService appVersionService;

    @Operation(summary = "APP版本分页列表")
    @GetMapping("/page")
    public PageResult<AppVersionVO> getPage(@ParameterObject AppVersionPageQuery queryParams) {
        Page<AppVersionVO> result = appVersionService.getPage(queryParams);
        return PageResult.success(result);
    }

    @Operation(summary = "APP版本详情")
    @GetMapping("/{id}")
    public Result<AppVersionVO> getDetail(@Parameter(description = "版本ID") @PathVariable Long id) {
        return Result.success(appVersionService.getDetail(id));
    }

    @Operation(summary = "新增APP版本")
    @PostMapping
    @PreAuthorize("@ss.hasPerm('sys:app_version:add')")
    @PreventDuplicateResubmit
    public Result<Boolean> add(@RequestBody AppVersionForm form) {
        return Result.success(appVersionService.add(form));
    }

    @Operation(summary = "修改APP版本")
    @PutMapping("/{id}")
    @PreAuthorize("@ss.hasPerm('sys:app_version:edit')")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody AppVersionForm form) {
        return Result.success(appVersionService.update(id, form));
    }

    @Operation(summary = "删除APP版本")
    @DeleteMapping("/{ids}")
    @PreAuthorize("@ss.hasPerm('sys:app_version:delete')")
    public Result<Boolean> delete(@Parameter(description = "版本ID，多个以英文逗号(,)拼接") @PathVariable String ids) {
        return Result.success(appVersionService.delete(ids));
    }
}
