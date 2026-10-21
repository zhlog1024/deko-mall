package com.deko.mall.cms.controller.feign;

import com.deko.mall.cms.api.dto.ArticleCategoryNodeDTO;
import com.deko.mall.cms.model.vo.ArticleCategoryVO;
import com.deko.mall.cms.service.ArticleCategoryService;
import com.deko.mall.common.result.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 文章分类内部 Feign 接口（供装修选择器）
 */
@Tag(name = "feign-文章分类内部接口")
@RestController
@RequestMapping("/feign-api/v1/article-categories")
@RequiredArgsConstructor
public class ArticleCategoryFeignController {

    private final ArticleCategoryService articleCategoryService;

    @GetMapping("/tree")
    public Result<List<ArticleCategoryNodeDTO>> getEnabledTree() {
        List<ArticleCategoryVO> tree = articleCategoryService.tree(true);
        return Result.success(convert(tree));
    }

    private List<ArticleCategoryNodeDTO> convert(List<ArticleCategoryVO> tree) {
        return tree.stream().map(vo -> {
            ArticleCategoryNodeDTO dto = new ArticleCategoryNodeDTO();
            dto.setId(vo.getId());
            dto.setPid(vo.getPid());
            dto.setName(vo.getName());
            dto.setChildren(convert(vo.getChildren()));
            return dto;
        }).toList();
    }
}
