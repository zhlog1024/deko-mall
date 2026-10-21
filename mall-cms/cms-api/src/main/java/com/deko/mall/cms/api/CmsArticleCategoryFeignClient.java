package com.deko.mall.cms.api;

import com.deko.mall.cms.api.dto.ArticleCategoryNodeDTO;
import com.deko.mall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * 文章分类 Feign 客户端（装修器分类选择器数据源）
 */
@FeignClient(value = "mall-cms", contextId = "cmsArticleCategory")
public interface CmsArticleCategoryFeignClient {

    /**
     * 启用状态文章分类树（含子级）
     *
     * @return 分类树
     */
    @GetMapping("/feign-api/v1/article-categories/tree")
    Result<List<ArticleCategoryNodeDTO>> getEnabledTree();
}
