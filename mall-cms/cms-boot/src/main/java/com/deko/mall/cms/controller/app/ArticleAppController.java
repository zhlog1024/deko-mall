package com.deko.mall.cms.controller.app;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.deko.mall.cms.model.query.ArticleAppQuery;
import com.deko.mall.cms.model.vo.ArticleAppDetailVO;
import com.deko.mall.cms.model.vo.ArticleAppItemVO;
import com.deko.mall.cms.model.vo.ArticleCategoryVO;
import com.deko.mall.cms.service.ArticleCategoryService;
import com.deko.mall.cms.service.ArticleService;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * APP端-文章接口
 */
@Tag(name = "App-文章接口", description = "文章分类树、列表、详情")
@RestController
@RequestMapping("/app-api/v1/article")
@RequiredArgsConstructor
public class ArticleAppController {

    private final ArticleService articleService;
    private final ArticleCategoryService articleCategoryService;

    @Operation(summary = "启用分类树")
    @GetMapping("/category-tree")
    public Result<List<ArticleCategoryVO>> categoryTree() {
        return Result.success(articleCategoryService.tree(true));
    }

    @Operation(summary = "文章分页列表", description = "启用状态；分类含子级；排序0综合/1人气/2最新")
    @GetMapping("/list")
    public PageResult<ArticleAppItemVO> list(ArticleAppQuery query) {
        IPage<ArticleAppItemVO> page = articleService.appList(query);
        return PageResult.success(page);
    }

    @Operation(summary = "文章详情", description = "访问量+1")
    @GetMapping("/{id}")
    public Result<ArticleAppDetailVO> detail(@Parameter(description = "文章ID") @PathVariable Long id) {
        return Result.success(articleService.appDetail(id));
    }
}
