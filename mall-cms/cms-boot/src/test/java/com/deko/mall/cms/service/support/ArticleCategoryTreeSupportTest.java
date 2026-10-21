package com.deko.mall.cms.service.support;

import com.deko.mall.cms.model.entity.ArticleCategory;
import com.deko.mall.cms.model.vo.ArticleCategoryVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 文章分类树纯逻辑单元测试
 */
class ArticleCategoryTreeSupportTest {

    private ArticleCategory category(Long id, Long pid, String name, int sort) {
        ArticleCategory c = new ArticleCategory();
        c.setId(id);
        c.setPid(pid);
        c.setName(name);
        c.setSort(sort);
        return c;
    }

    @Test
    @DisplayName("空列表构建树返回空")
    void toTreeWithEmpty() {
        assertTrue(ArticleCategoryTreeSupport.toTree(List.of()).isEmpty());
    }

    @Test
    @DisplayName("两级分类构建树,根含子级且按sort排序")
    void toTreeWithTwoLevels() {
        List<ArticleCategory> all = List.of(
                category(2L, 1L, "子帮助", 0),
                category(3L, 0L, "资讯", 1),
                category(1L, 0L, "帮助中心", 0)
        );
        List<ArticleCategoryVO> tree = ArticleCategoryTreeSupport.toTree(all);
        assertEquals(2, tree.size());
        // 根按 sort 升序:帮助中心(sort0) 在前,资讯(sort1) 在后
        assertEquals(1L, tree.get(0).getId());
        assertEquals(3L, tree.get(1).getId());
        // 帮助中心下挂"子帮助"
        assertEquals(1, tree.get(0).getChildren().size());
        assertEquals(2L, tree.get(0).getChildren().get(0).getId());
        // 资讯无子级
        assertTrue(tree.get(1).getChildren().isEmpty());
    }

    @Test
    @DisplayName("孤儿节点(父不存在)提升为根")
    void toTreeWithOrphan() {
        List<ArticleCategory> all = List.of(category(5L, 99L, "孤儿", 0));
        List<ArticleCategoryVO> tree = ArticleCategoryTreeSupport.toTree(all);
        assertEquals(1, tree.size());
        assertEquals(5L, tree.get(0).getId());
    }

    @Test
    @DisplayName("三层分类递归展开含自身与全部后代")
    void collectDescendantIdsMultiLevel() {
        List<ArticleCategory> all = List.of(
                category(1L, 0L, "顶级", 0),
                category(2L, 1L, "二级A", 0),
                category(3L, 1L, "二级B", 0),
                category(4L, 2L, "三级A", 0),
                category(5L, 2L, "三级B", 0),
                category(6L, 0L, "其它", 0)
        );
        Set<Long> ids = ArticleCategoryTreeSupport.collectDescendantIds(all, 1L);
        assertEquals(Set.of(1L, 2L, 3L, 4L, 5L), ids);
    }

    @Test
    @DisplayName("叶子分类仅返回自身")
    void collectDescendantIdsLeaf() {
        List<ArticleCategory> all = List.of(
                category(1L, 0L, "顶级", 0),
                category(7L, 1L, "叶子", 0)
        );
        assertEquals(Set.of(7L), ArticleCategoryTreeSupport.collectDescendantIds(all, 7L));
    }

    @Test
    @DisplayName("目标分类不存在或为null返回空集")
    void collectDescendantIdsNotExist() {
        List<ArticleCategory> all = List.of(category(1L, 0L, "顶级", 0));
        assertTrue(ArticleCategoryTreeSupport.collectDescendantIds(all, 999L).isEmpty());
        assertTrue(ArticleCategoryTreeSupport.collectDescendantIds(all, (Long) null).isEmpty());
    }

    @Test
    @DisplayName("多根分类展开取并集去重,不存在的根被忽略")
    void collectDescendantIdsMultiRoot() {
        List<ArticleCategory> all = List.of(
                category(1L, 0L, "顶级", 0),
                category(2L, 1L, "二级A", 0),
                category(3L, 1L, "二级B", 0),
                category(4L, 2L, "三级", 0),
                category(5L, 0L, "其它", 0),
                category(6L, 5L, "其它子", 0)
        );
        // 根1(含2/3/4) 与 根5(含6) 并集;999 不存在被忽略;根1重复无影响
        Set<Long> ids = ArticleCategoryTreeSupport.collectDescendantIds(
                all, List.of(1L, 5L, 1L, 999L));
        assertEquals(Set.of(1L, 2L, 3L, 4L, 5L, 6L), ids);
        // 空集合返回空
        assertTrue(ArticleCategoryTreeSupport.collectDescendantIds(all, List.of()).isEmpty());
    }
}
