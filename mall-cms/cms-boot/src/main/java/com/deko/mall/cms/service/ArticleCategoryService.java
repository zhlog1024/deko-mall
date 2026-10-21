package com.deko.mall.cms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.cms.model.dto.ArticleCategorySaveDTO;
import com.deko.mall.cms.model.entity.ArticleCategory;
import com.deko.mall.cms.model.vo.ArticleCategoryVO;

import java.util.Collection;
import java.util.List;

/**
 * 文章分类服务
 */
public interface ArticleCategoryService extends IService<ArticleCategory> {

    /**
     * 分类树（管理端含停用；选择器仅启用）
     *
     * @param enabledOnly 是否仅启用
     * @return 分类树
     */
    List<ArticleCategoryVO> tree(boolean enabledOnly);

    /**
     * 新增分类
     */
    boolean createCategory(ArticleCategorySaveDTO dto);

    /**
     * 编辑分类
     */
    boolean updateCategory(ArticleCategorySaveDTO dto);

    /**
     * 删除分类（有子级或文章引用时拒绝）
     */
    boolean deleteCategory(Long id);

    /**
     * 展开分类含子级 id（用于"含子级"文章列表查询；输入分类须启用未删）
     *
     * @param categoryId 分类id
     * @return 自身及后代 id 列表
     */
    List<Long> expandCategoryIds(Long categoryId);

    /**
     * 批量展开多个分类含子级 id 的并集（去重；多分类筛选用，一次性加载启用分类）
     *
     * @param categoryIds 分类id集合
     * @return 各根自身及后代 id 的并集
     */
    List<Long> expandCategoryIds(Collection<Long> categoryIds);
}
