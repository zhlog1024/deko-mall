package com.deko.mall.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.cms.api.dto.ArticleAutoQuery;
import com.deko.mall.cms.api.dto.ArticleItemDTO;
import com.deko.mall.cms.mapper.ArticleCategoryMapper;
import com.deko.mall.cms.mapper.ArticleMapper;
import com.deko.mall.cms.model.dto.ArticleSaveDTO;
import com.deko.mall.cms.model.entity.Article;
import com.deko.mall.cms.model.entity.ArticleCategory;
import com.deko.mall.cms.model.query.ArticleAppQuery;
import com.deko.mall.cms.model.query.ArticlePageQuery;
import com.deko.mall.cms.model.vo.ArticleAppDetailVO;
import com.deko.mall.cms.model.vo.ArticleAppItemVO;
import com.deko.mall.cms.model.vo.ArticleDetailVO;
import com.deko.mall.cms.model.vo.ArticleVO;
import com.deko.mall.cms.service.ArticleCategoryService;
import com.deko.mall.cms.service.ArticleService;
import com.deko.mall.cms.service.support.ArticleSortSupport;
import com.deko.mall.common.web.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文章服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    private final ArticleCategoryMapper categoryMapper;
    private final ArticleCategoryService categoryService;

    @Override
    public IPage<ArticleVO> pageArticles(ArticlePageQuery query) {
        Page<Article> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Article> wrapper = buildBaseWrapper();
        if (StringUtils.isNotBlank(query.getKeywords())) {
            String kw = query.getKeywords();
            wrapper.and(w -> w.like(Article::getTitle, kw).or().like(Article::getDescription, kw));
        }
        if (query.getCategoryId() != null) {
            wrapper.eq(Article::getCategoryId, query.getCategoryId());
        }
        if (query.getIsEnable() != null) {
            wrapper.eq(Article::getIsEnable, query.getIsEnable());
        }
        wrapper.orderByDesc(Article::getId);

        IPage<Article> result = page(page, wrapper);
        List<ArticleVO> voList = result.getRecords().stream().map(this::toArticleVO).toList();
        fillCategoryName(voList);

        Page<ArticleVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public ArticleDetailVO getDetail(Long id) {
        Article article = requireArticle(id, false);
        ArticleDetailVO vo = new ArticleDetailVO();
        BeanUtils.copyProperties(article, vo);
        fillCategoryName(List.of(vo));
        return vo;
    }

    @Override
    public boolean createArticle(ArticleSaveDTO dto) {
        requireCategory(dto.getCategoryId());
        Article article = new Article();
        BeanUtils.copyProperties(dto, article);
        if (article.getIsEnable() == null) {
            article.setIsEnable(1);
        }
        article.setAccessCount(0);
        article.setDeleted(0);
        return save(article);
    }

    @Override
    public boolean updateArticle(ArticleSaveDTO dto) {
        if (dto.getId() == null) {
            throw new BizException("文章ID不能为空");
        }
        requireArticle(dto.getId(), false);
        // 局部更新支持:未传分类时不强制校验
        if (dto.getCategoryId() != null) {
            requireCategory(dto.getCategoryId());
        }
        Article article = new Article();
        BeanUtils.copyProperties(dto, article);
        return updateById(article);
    }

    @Override
    public boolean deleteArticle(Long id) {
        Article upd = new Article();
        upd.setId(id);
        upd.setDeleted(1);
        return updateById(upd);
    }

    @Override
    public IPage<ArticleAppItemVO> appList(ArticleAppQuery query) {
        Page<Article> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Article> wrapper = buildEnabledWrapper(query);
        applyOrder(wrapper, query.getOrderByType(), query.getOrderByRule());

        IPage<Article> result = page(page, wrapper);
        List<ArticleAppItemVO> voList = result.getRecords().stream().map(this::toAppItemVO).toList();
        Page<ArticleAppItemVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public ArticleAppDetailVO appDetail(Long id) {
        Article article = requireArticle(id, true);
        // 访问量自增（SQL 原子更新，不触发字段自动填充）
        UpdateWrapper<Article> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id).setSql("access_count = access_count + 1");
        update(updateWrapper);

        ArticleAppDetailVO vo = new ArticleAppDetailVO();
        BeanUtils.copyProperties(article, vo);
        return vo;
    }

    @Override
    public List<ArticleItemDTO> listByIdsFeign(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<Article> wrapper = buildBaseWrapper();
        wrapper.in(Article::getId, ids).eq(Article::getIsEnable, 1);
        return list(wrapper).stream().map(this::toFeignItem).toList();
    }

    @Override
    public List<ArticleItemDTO> autoListFeign(ArticleAutoQuery query) {
        LambdaQueryWrapper<Article> wrapper = buildBaseWrapper();
        wrapper.eq(Article::getIsEnable, 1);
        if (query.getCategoryIds() != null && !query.getCategoryIds().isEmpty()) {
            List<Long> expanded = categoryService.expandCategoryIds(query.getCategoryIds());
            if (expanded.isEmpty()) {
                return List.of();
            }
            wrapper.in(Article::getCategoryId, expanded);
        }
        if (query.getOnlyCover() != null && query.getOnlyCover() == 1) {
            wrapper.ne(Article::getCover, "");
        }
        if (StringUtils.isNotBlank(query.getKeyword())) {
            String kw = query.getKeyword();
            wrapper.and(w -> w.like(Article::getTitle, kw).or().like(Article::getDescription, kw));
        }
        applyOrder(wrapper, query.getOrderByType(), query.getOrderByRule());
        int number = query.getNumber() == null ? 10 : Math.min(Math.max(query.getNumber(), 1), 50);
        wrapper.last("LIMIT " + number);
        return list(wrapper).stream().map(this::toFeignItem).toList();
    }

    // 私有工具

    private LambdaQueryWrapper<Article> buildBaseWrapper() {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getDeleted, 0);
        return wrapper;
    }

    private LambdaQueryWrapper<Article> buildEnabledWrapper(ArticleAppQuery query) {
        LambdaQueryWrapper<Article> wrapper = buildBaseWrapper();
        wrapper.eq(Article::getIsEnable, 1);
        List<Long> categoryRoots = new ArrayList<>();
        if (query.getCategoryId() != null) {
            categoryRoots.add(query.getCategoryId());
        }
        if (query.getCategoryIds() != null) {
            for (Long cid : query.getCategoryIds()) {
                if (cid != null) {
                    categoryRoots.add(cid);
                }
            }
        }
        if (!categoryRoots.isEmpty()) {
            List<Long> ids = categoryService.expandCategoryIds(categoryRoots);
            if (ids.isEmpty()) {
                wrapper.eq(Article::getId, -1L); // 空集占位，保证无结果
            } else {
                wrapper.in(Article::getCategoryId, ids);
            }
        }
        Integer onlyCover = query.getOnlyCover();
        if (onlyCover != null && onlyCover == 1) {
            wrapper.ne(Article::getCover, "");
        }
        String keyword = query.getKeyword();
        if (StringUtils.isNotBlank(keyword)) {
            String kw = keyword;
            wrapper.and(w -> w.like(Article::getTitle, kw).or().like(Article::getDescription, kw));
        }
        return wrapper;
    }

    private void applyOrder(LambdaQueryWrapper<Article> wrapper, Integer orderByType, String orderByRule) {
        String[] columns = ArticleSortSupport.resolveOrderColumns(orderByType);
        boolean asc = "asc".equals(ArticleSortSupport.resolveRule(orderByRule));
        for (String column : columns) {
            switch (column) {
                case "access_count" -> wrapper.orderBy(true, asc, Article::getAccessCount);
                case "id" -> wrapper.orderBy(true, asc, Article::getId);
                default -> {
                }
            }
        }
    }

    private Article requireArticle(Long id, boolean enabledOnly) {
        Article article = getById(id);
        if (article == null || article.getDeleted() != null && article.getDeleted() == 1) {
            throw new BizException("文章不存在");
        }
        if (enabledOnly && (article.getIsEnable() == null || article.getIsEnable() != 1)) {
            throw new BizException("文章已下架");
        }
        return article;
    }

    private void requireCategory(Long categoryId) {
        ArticleCategory category = categoryMapper.selectById(categoryId);
        if (category == null || category.getDeleted() != null && category.getDeleted() == 1) {
            throw new BizException("文章分类不存在");
        }
    }

    private ArticleVO toArticleVO(Article article) {
        ArticleVO vo = new ArticleVO();
        BeanUtils.copyProperties(article, vo);
        return vo;
    }

    private ArticleAppItemVO toAppItemVO(Article article) {
        ArticleAppItemVO vo = new ArticleAppItemVO();
        BeanUtils.copyProperties(article, vo);
        return vo;
    }

    private ArticleItemDTO toFeignItem(Article article) {
        ArticleItemDTO dto = new ArticleItemDTO();
        BeanUtils.copyProperties(article, dto);
        return dto;
    }

    /**
     * 填充分类名称（批量一次查询）
     */
    private void fillCategoryName(List<? extends ArticleVO> targets) {
        if (targets == null || targets.isEmpty()) {
            return;
        }
        List<Long> ids = targets.stream().map(ArticleVO::getCategoryId)
                .filter(java.util.Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return;
        }
        Map<Long, String> nameMap = categoryMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(ArticleCategory::getId, ArticleCategory::getName, (a, b) -> a));
        for (ArticleVO vo : targets) {
            if (vo.getCategoryId() != null && nameMap.containsKey(vo.getCategoryId())) {
                vo.setCategoryName(nameMap.get(vo.getCategoryId()));
            }
        }
    }
}
