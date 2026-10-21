package com.deko.mall.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import com.deko.mall.system.model.entity.SysMsgTemplate;
import com.deko.mall.system.model.form.SysMsgTemplateForm;
import com.deko.mall.system.model.vo.SysMsgTemplatePageVO;
import com.deko.mall.system.service.msg.SysMsgTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "消息模板管理")
@RestController
@RequestMapping("/api/v1/msg/templates")
@RequiredArgsConstructor
public class SysMsgTemplateController {

    private final SysMsgTemplateService templateService;

    @Operation(summary = "分页列表")
    @GetMapping("/page")
    public PageResult<SysMsgTemplate> listTemplatesPage(
            @ParameterObject Page<SysMsgTemplate> page,
            @ParameterObject SysMsgTemplatePageVO queryVO
    ) {
        Page<SysMsgTemplate> result = templateService.listTemplatesPage(page, queryVO);
        return PageResult.success(result);
    }

    @Operation(summary = "模板详情")
    @GetMapping("/{templateCode}")
    public Result<SysMsgTemplate> getTemplate(
            @Parameter(description = "模板编码") @PathVariable String templateCode
    ) {
        SysMsgTemplate template = templateService.getTemplate(templateCode);
        return Result.success(template);
    }

    @Operation(summary = "新增模板")
    @PostMapping
    @PreAuthorize("@pms.hasPermission('sys:msg:template:add')")
    public Result<Long> createTemplate(
            @Valid @RequestBody SysMsgTemplateForm form
    ) {
        Long id = templateService.createTemplate(form);
        return Result.success(id);
    }

    @Operation(summary = "修改模板")
    @PutMapping("/{id}")
    @PreAuthorize("@pms.hasPermission('sys:msg:template:update')")
    public Result<Boolean> updateTemplate(
            @Parameter(description = "模板ID") @PathVariable Long id,
            @Valid @RequestBody SysMsgTemplateForm form
    ) {
        form.setId(id);
        boolean result = templateService.updateTemplate(form);
        return Result.judge(result);
    }

    @Operation(summary = "删除模板")
    @DeleteMapping("/{ids}")
    @PreAuthorize("@pms.hasPermission('sys:msg:template:delete')")
    public Result<Boolean> deleteTemplates(
            @Parameter(description = "模板ID，多个以英文逗号(,)分割") @PathVariable Long[] ids
    ) {
        boolean result = templateService.deleteTemplates(ids);
        return Result.judge(result);
    }
}
