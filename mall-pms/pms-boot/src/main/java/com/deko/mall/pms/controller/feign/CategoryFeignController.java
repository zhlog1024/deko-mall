package com.deko.mall.pms.controller.feign;

import com.deko.mall.common.result.Result;
import com.deko.mall.pms.api.CategoryFeignClient;
import com.deko.mall.pms.model.vo.CategoryVO;
import com.deko.mall.pms.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Feign-商品分类控制层
 */
@Tag(name = "feign-商品分类接口")
@RestController
@RequestMapping("/feign-api/v1/categories")
@AllArgsConstructor
@Slf4j
public class CategoryFeignController implements CategoryFeignClient {

    private CategoryService categoryService;

    @Operation(summary = "获取分类列表（树形结构）")
    @GetMapping("/tree")
    @Override
    public Result<List<CategoryVO>> getCategoryTree(@RequestParam(required = false) Long parentId) {
        List<CategoryVO> list = categoryService.getAppCategoryList(parentId);
        return Result.success(list);
    }

    @Operation(summary = "获取所有分类（扁平列表）")
    @GetMapping("/all")
    @Override
    public Result<List<CategoryVO>> getAllCategories() {
        List<CategoryVO> list = categoryService.getAppCategoryList(null);
        return Result.success(list);
    }
}

