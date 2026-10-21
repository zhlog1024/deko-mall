package com.deko.mall.system.controller;

import com.deko.mall.common.result.Result;
import com.deko.mall.system.model.form.SysAttachmentCategoryForm;
import com.deko.mall.system.model.vo.SysAttachmentCategoryVO;
import com.deko.mall.system.service.SysAttachmentCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 附件分类控制器
 */
@Tag(name = "附件分类管理")
@RestController
@RequestMapping("/api/v1/attachments/categories")
@RequiredArgsConstructor
public class SysAttachmentCategoryController {

    private final SysAttachmentCategoryService categoryService;

    @Operation(summary = "获取分类树")
    @GetMapping
    public Result<List<SysAttachmentCategoryVO>> getCategoryTree() {
        List<SysAttachmentCategoryVO> tree = categoryService.getCategoryTree();
        return Result.success(tree);
    }

    @Operation(summary = "保存分类（新增/编辑）")
    @PostMapping
    public Result<Long> saveCategory(@Valid @RequestBody SysAttachmentCategoryForm form) {
        Long id = categoryService.saveCategory(form);
        return Result.success(id);
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(
            @Parameter(description = "分类ID") @PathVariable Long id
    ) {
        categoryService.deleteCategory(id);
        return Result.success();
    }

    @Operation(summary = "移动附件到其他分类")
    @PostMapping("/move-category")
    public Result<Void> moveAttachmentsToCategory(
            @RequestBody MoveCategoryRequest request
    ) {
        categoryService.moveAttachmentsToCategory(request.getAttachmentIds(), request.getTargetCategoryId());
        return Result.success();
    }

    /**
     * 移动分类请求对象
     */
    @lombok.Data
    static class MoveCategoryRequest {
        private List<Long> attachmentIds;
        private Long targetCategoryId;
    }
}

