package com.deko.mall.pms.api;

import com.deko.mall.common.result.Result;
import com.deko.mall.common.web.config.FeignDecoderConfig;
import com.deko.mall.pms.model.vo.CategoryVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 商品分类 Feign 客户端
 */
@FeignClient(value = "mall-pms", contextId = "category", configuration = {FeignDecoderConfig.class})
public interface CategoryFeignClient {

    /**
     * 获取分类列表（树形结构）
     *
     * @param parentId 父分类ID，可选
     * @return 分类列表
     */
    @GetMapping("/feign-api/v1/categories/tree")
    Result<List<CategoryVO>> getCategoryTree(@RequestParam(required = false) Long parentId);

    /**
     * 获取所有分类（扁平列表）
     *
     * @return 所有分类列表
     */
    @GetMapping("/feign-api/v1/categories/all")
    Result<List<CategoryVO>> getAllCategories();
}

