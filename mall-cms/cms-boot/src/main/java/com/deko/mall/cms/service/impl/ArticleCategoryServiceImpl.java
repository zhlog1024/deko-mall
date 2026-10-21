package com.deko.mall.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.cms.mapper.ArticleCategoryMapper;
import com.deko.mall.cms.mapper.ArticleMapper;
import com.deko.mall.cms.model.dto.ArticleCategorySaveDTO;
import com.deko.mall.cms.model.entity.ArticleCategory;
import com.deko.mall.cms.model.vo.ArticleCategoryVO;
import com.deko.mall.cms.service.ArticleCategoryService;
import com.deko.mall.cms.service.support.ArticleCategoryTreeSupport;
import com.deko.mall.common.web.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 文章分类服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleCategoryServiceImpl extends ServiceImpl<ArticleCategoryMapper, ArticleCategory>
        implements ArticleCategoryService {

    private final ArticleMapper articleMapper;

    @Override
    public List<ArticleCategoryVO> tree(boolean enabledOnly) {
        LambdaQueryWrapper<ArticleCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleCategory::getDeleted, 0);
        if (enabledOnly) {
            wrapper.eq(ArticleCategory::getIsEnable, 1);
        }
        List<ArticleCategory> all = list(wrapper);
        return ArticleCategoryTreeSupport.toTree(all);
    }

    @Override
    public boolean createCategory(ArticleCategorySaveDTO dto) {
        validatePid(dto.getPid());
        ArticleCategory category = new ArticleCategory();
        BeanUtils.copyProperties(dto, category);
        if (category.getPid() == null) {
            category.setPid(0L);
        }
        if (category.getSort() == null) {
            category.setSort(0);
        }
        if (category.getIsEnable() == null) {
            category.setIsEnable(1);
        }
        category.setDeleted(0);
        return save(category);
    }

    @Override
    public boolean updateCategory(ArticleCategorySaveDTO dto) {
        if (dto.getId() == null) {
            throw new BizException("分类ID不能为空");
        }
        if (dto.getId().equals(dto.getPid())) {
            throw new BizException("父分类不能是自己");
        }
        ArticleCategory exist = getById(dto.getId());
        if (exist == null || exist.getDeleted() != null && exist.getDeleted() == 1) {
            throw new BizException("分类不存在");
        }
        validatePid(dto.getPid());
        ArticleCategory category = new ArticleCategory();
        BeanUtils.copyProperties(dto, category);
        return updateById(category);
    }

    @Override
    public boolean deleteCategory(Long id) {
        if (id == null) {
            throw new BizException("分类ID不能为空");
        }
        Long childCount = lambdaQuery().eq(ArticleCategory::getPid, id)
                .eq(ArticleCategory::getDeleted, 0).count();
        if (childCount != null && childCount > 0) {
            throw new BizException("存在子分类，请先删除子分类");
        }
        LambdaQueryWrapper<com.deko.mall.cms.model.entity.Article> articleWrapper =
                new LambdaQueryWrapper<com.deko.mall.cms.model.entity.Article>()
                        .eq(com.deko.mall.cms.model.entity.Article::getCategoryId, id)
                        .eq(com.deko.mall.cms.model.entity.Article::getDeleted, 0);
        Long articleCount = articleMapper.selectCount(articleWrapper);
        if (articleCount != null && articleCount > 0) {
            throw new BizException("分类下存在文章，请先移走文章");
        }
        ArticleCategory upd = new ArticleCategory();
        upd.setId(id);
        upd.setDeleted(1);
        return updateById(upd);
    }

    @Override
    public List<Long> expandCategoryIds(Long categoryId) {
        if (categoryId == null) {
            return List.of();
        }
        return expandCategoryIds(List.of(categoryId));
    }

    @Override
    public List<Long> expandCategoryIds(Collection<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<ArticleCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleCategory::getDeleted, 0).eq(ArticleCategory::getIsEnable, 1);
        List<ArticleCategory> all = list(wrapper);
        Set<Long> ids = ArticleCategoryTreeSupport.collectDescendantIds(all, categoryIds);
        return new ArrayList<>(ids);
    }

    private void validatePid(Long pid) {
        if (pid == null || pid == 0L) {
            return;
        }
        ArticleCategory parent = getById(pid);
        if (parent == null || parent.getDeleted() != null && parent.getDeleted() == 1) {
            throw new BizException("父分类不存在");
        }
    }
}
