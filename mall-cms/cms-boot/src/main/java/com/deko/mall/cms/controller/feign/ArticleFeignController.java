package com.deko.mall.cms.controller.feign;

import com.deko.mall.cms.api.dto.ArticleAutoQuery;
import com.deko.mall.cms.api.dto.ArticleByIdsQuery;
import com.deko.mall.cms.api.dto.ArticleItemDTO;
import com.deko.mall.cms.service.ArticleService;
import com.deko.mall.common.result.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 文章内部 Feign 接口（供装修桥接等）
 */
@Tag(name = "feign-文章内部接口")
@RestController
@RequestMapping("/feign-api/v1/articles")
@RequiredArgsConstructor
public class ArticleFeignController {

    private final ArticleService articleService;

    @PostMapping("/by-ids")
    public Result<List<ArticleItemDTO>> listByIds(@RequestBody ArticleByIdsQuery query) {
        return Result.success(articleService.listByIdsFeign(query == null ? null : query.getIds()));
    }

    @PostMapping("/auto-list")
    public Result<List<ArticleItemDTO>> autoList(@RequestBody ArticleAutoQuery query) {
        return Result.success(articleService.autoListFeign(query));
    }
}
