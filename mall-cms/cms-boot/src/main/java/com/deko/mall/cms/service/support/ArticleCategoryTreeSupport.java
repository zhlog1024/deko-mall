package com.deko.mall.cms.service.support;

import com.deko.mall.cms.model.entity.ArticleCategory;
import com.deko.mall.cms.model.vo.ArticleCategoryVO;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 文章分类树纯逻辑支持（无 Spring 依赖，可单元测试）
 * <p>输入分类列表须已过滤 deleted；树构建按 sort,id 升序。
 */
public final class ArticleCategoryTreeSupport {

    private ArticleCategoryTreeSupport() {
    }

    /**
     * 扁平分类列表 → 树（pid=0 为根；按 sort,id 升序）
     */
    public static List<ArticleCategoryVO> toTree(List<ArticleCategory> all) {
        List<ArticleCategory> sorted = new ArrayList<>(all);
        sorted.sort(Comparator.comparing(ArticleCategory::getSort, Comparator.nullsFirst(Integer::compareTo))
                .thenComparing(ArticleCategory::getId, Comparator.nullsFirst(Long::compareTo)));

        Map<Long, ArticleCategoryVO> voMap = new HashMap<>();
        for (ArticleCategory category : sorted) {
            ArticleCategoryVO vo = new ArticleCategoryVO();
            BeanUtils.copyProperties(category, vo);
            voMap.put(category.getId(), vo);
        }

        List<ArticleCategoryVO> roots = new ArrayList<>();
        for (ArticleCategory category : sorted) {
            ArticleCategoryVO vo = voMap.get(category.getId());
            ArticleCategoryVO parent = category.getPid() == null ? null : voMap.get(category.getPid());
            if (parent == null) {
                roots.add(vo);
            } else {
                parent.getChildren().add(vo);
            }
        }
        return roots;
    }

    /**
     * 收集指定分类及其所有子孙分类 id（含自身；用于"含子级"列表查询）
     *
     * @param all    全部启用分类（已过滤 deleted/is_enable）
     * @param rootId 目标分类 id
     * @return id 集合（含自身与后代）
     */
    public static Set<Long> collectDescendantIds(List<ArticleCategory> all, Long rootId) {
        if (rootId == null) {
            return Set.of();
        }
        return collectDescendantIds(all, List.of(rootId));
    }

    /**
     * 收集多个分类及其所有子孙分类 id 的并集（含自身；用于多分类"含子级"列表查询）
     *
     * @param all     全部启用分类（已过滤 deleted/is_enable）
     * @param rootIds 目标分类 id 集合（不存在于 all 的根会被忽略）
     * @return id 并集（含各根自身与后代，去重）
     */
    public static Set<Long> collectDescendantIds(List<ArticleCategory> all, Collection<Long> rootIds) {
        if (rootIds == null || rootIds.isEmpty()) {
            return Set.of();
        }
        Map<Long, List<Long>> childrenMap = new HashMap<>();
        for (ArticleCategory category : all) {
            if (category.getId() == null) {
                continue;
            }
            Long pid = category.getPid() == null ? 0L : category.getPid();
            childrenMap.computeIfAbsent(pid, k -> new ArrayList<>()).add(category.getId());
        }
        Set<Long> result = new HashSet<>();
        for (Long rootId : rootIds) {
            if (rootId == null) {
                continue;
            }
            boolean rootExists = all.stream().anyMatch(c -> rootId.equals(c.getId()));
            if (rootExists) {
                dfs(childrenMap, rootId, result);
            }
        }
        return result;
    }

    private static void dfs(Map<Long, List<Long>> childrenMap, Long nodeId, Set<Long> result) {
        if (nodeId == null || !result.add(nodeId)) {
            return;
        }
        List<Long> children = childrenMap.get(nodeId);
        if (children != null) {
            for (Long child : children) {
                dfs(childrenMap, child, result);
            }
        }
    }
}
