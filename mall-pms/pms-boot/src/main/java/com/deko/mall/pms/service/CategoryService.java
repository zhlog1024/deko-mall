package com.deko.mall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.common.web.model.Option;
import com.deko.mall.pms.model.entity.PmsCategory;
import com.deko.mall.pms.model.vo.CategoryVO;

import java.util.List;


/**
 * 商品分类
 */
public interface CategoryService extends IService<PmsCategory> {


    /**
     * 分类列表（树形）
     */
    List<CategoryVO> getCategoryList(Long parentId);

    List<CategoryVO> getAppCategoryList(Long parentId);

    /**
     * 分类列表（级联）
     */
    List<Option> getCategoryOptions();


    /**
     * 保存（新增/修改）分类
     */
    Long saveCategory(PmsCategory category);

}
