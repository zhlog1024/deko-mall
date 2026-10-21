package com.deko.mall.cms.api;

import com.deko.mall.cms.api.dto.ArticleAutoQuery;
import com.deko.mall.cms.api.dto.ArticleByIdsQuery;
import com.deko.mall.cms.api.dto.ArticleItemDTO;
import com.deko.mall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 文章服务 Feign 客户端（供装修桥接、未来官网等消费）
 */
@FeignClient(value = "mall-cms", contextId = "cmsArticle")
public interface CmsArticleFeignClient {

    /**
     * 指定文章数据（装修器"指定数据"模式：按 ids 拉取）
     *
     * @param query 文章ID列表
     * @return 文章数据项
     */
    @PostMapping("/feign-api/v1/articles/by-ids")
    Result<List<ArticleItemDTO>> listByIds(@RequestBody ArticleByIdsQuery query);

    /**
     * 自动文章数据（分类筛选 + 数量 + 排序，含子分类递归）
     *
     * @param query 自动数据查询
     * @return 文章数据项
     */
    @PostMapping("/feign-api/v1/articles/auto-list")
    Result<List<ArticleItemDTO>> autoList(@RequestBody ArticleAutoQuery query);
}
