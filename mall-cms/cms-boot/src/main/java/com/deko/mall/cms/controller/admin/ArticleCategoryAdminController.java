package com.deko.mall.cms.controller.admin;

import com.deko.mall.cms.model.dto.ArticleCategorySaveDTO;
import com.deko.mall.cms.model.vo.ArticleCategoryVO;
import com.deko.mall.cms.service.ArticleCategoryService;
import com.deko.mall.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端-文章分类接口
 */
@Tag(name = "admin-文章分类管理接口")
@RestController
@RequestMapping("/api/v1/article-category")
@RequiredArgsConstructor
public class ArticleCategoryAdminController {

    private final ArticleCategoryService articleCategoryService;

    @Operation(summary = "文章分类树(含停用,管理用)")
    @GetMapping("/tree")
    public Result<List<ArticleCategoryVO>> tree() {
        return Result.success(articleCategoryService.tree(false));
    }

    @Operation(summary = "新增分类")
    @PostMapping
    @PreAuthorize("@ss.hasPerm('cms:category:add')")
    public Result<Boolean> create(@Valid @RequestBody ArticleCategorySaveDTO dto) {
        return Result.judge(articleCategoryService.createCategory(dto));
    }

    @Operation(summary = "编辑分类", description = "支持局部字段更新(行内切换等)")
    @PutMapping("/{id}")
    @PreAuthorize("@ss.hasPerm('cms:category:edit')")
    public Result<Boolean> update(@Parameter(description = "分类ID") @PathVariable Long id,
                                  @RequestBody ArticleCategorySaveDTO dto) {
        dto.setId(id);
        return Result.judge(articleCategoryService.updateCategory(dto));
    }

    @Operation(summary = "删除分类(有子级/文章时拒绝)")
    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPerm('cms:category:delete')")
    public Result<Boolean> delete(@Parameter(description = "分类ID") @PathVariable Long id) {
        return Result.judge(articleCategoryService.deleteCategory(id));
    }
}
