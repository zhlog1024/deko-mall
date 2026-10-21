package com.deko.mall.cms.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.deko.mall.cms.model.dto.ArticleSaveDTO;
import com.deko.mall.cms.model.query.ArticlePageQuery;
import com.deko.mall.cms.model.vo.ArticleDetailVO;
import com.deko.mall.cms.model.vo.ArticleVO;
import com.deko.mall.cms.service.ArticleService;
import com.deko.mall.common.result.PageResult;
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

/**
 * 管理端-文章接口
 */
@Tag(name = "admin-文章管理接口")
@RestController
@RequestMapping("/api/v1/article")
@RequiredArgsConstructor
public class ArticleAdminController {

    private final ArticleService articleService;

    @Operation(summary = "文章分页列表")
    @GetMapping("/page")
    public PageResult<ArticleVO> pageArticles(ArticlePageQuery query) {
        IPage<ArticleVO> result = articleService.pageArticles(query);
        return PageResult.success(result);
    }

    @Operation(summary = "文章详情(编辑回显)")
    @GetMapping("/{id}")
    public Result<ArticleDetailVO> getDetail(@Parameter(description = "文章ID") @PathVariable Long id) {
        return Result.success(articleService.getDetail(id));
    }

    @Operation(summary = "新增文章")
    @PostMapping
    @PreAuthorize("@ss.hasPerm('cms:article:add')")
    public Result<Boolean> create(@Valid @RequestBody ArticleSaveDTO dto) {
        return Result.judge(articleService.createArticle(dto));
    }

    @Operation(summary = "编辑文章", description = "支持局部字段更新(行内切换等)")
    @PutMapping("/{id}")
    @PreAuthorize("@ss.hasPerm('cms:article:edit')")
    public Result<Boolean> update(@Parameter(description = "文章ID") @PathVariable Long id,
                                  @RequestBody ArticleSaveDTO dto) {
        dto.setId(id);
        return Result.judge(articleService.updateArticle(dto));
    }

    @Operation(summary = "删除文章(逻辑删除)")
    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPerm('cms:article:delete')")
    public Result<Boolean> delete(@Parameter(description = "文章ID") @PathVariable Long id) {
        return Result.judge(articleService.deleteArticle(id));
    }
}
