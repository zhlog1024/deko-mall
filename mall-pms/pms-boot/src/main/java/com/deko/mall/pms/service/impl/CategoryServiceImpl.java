package com.deko.mall.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.common.constant.GlobalConstants;
import com.deko.mall.common.web.model.Option;
import com.deko.mall.pms.mapper.PmsCategoryMapper;
import com.deko.mall.pms.model.entity.PmsCategory;
import com.deko.mall.pms.model.vo.CategoryVO;
import com.deko.mall.pms.service.CategoryService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品分类
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<PmsCategoryMapper, PmsCategory> implements CategoryService {

    /**
     * 缓存Key前缀
     */
    private static final String CATEGORY_CACHE_PREFIX = "pms:category:appList:";
    /**
     * 缓存过期时间（分钟）
     */
    private static final long CACHE_EXPIRE_MINUTES = 3;
    private final PmsCategoryMapper categoryMapper;
    private final RedisTemplate<String, Object> redisTemplate;


    public CategoryServiceImpl(PmsCategoryMapper categoryMapper, RedisTemplate<String, Object> redisTemplate) {
        this.categoryMapper = categoryMapper;
        this.redisTemplate = redisTemplate;
    }


    private static List<CategoryVO> recursionTree(Long parentId, List<PmsCategory> categoryList) {
        List<CategoryVO> list = new ArrayList<>();
        Optional.ofNullable(categoryList)
                .ifPresent(categories ->
                        categories.stream().filter(category ->
                                        category.getParentId().equals(parentId))
                                .forEach(category -> {
                                    CategoryVO categoryVO = new CategoryVO();
                                    BeanUtil.copyProperties(category, categoryVO);
                                    List<CategoryVO> children = recursionTree(category.getId(), categoryList);
                                    categoryVO.setChildren(children);
                                    list.add(categoryVO);
                                }));
        return list;
    }

    /**
     * 分类列表（树形）
     *
     * @Cacheable value:缓存名称(分区)；key：缓存键
     */
    // @Cacheable(value = "pms", key = "'categoryList'")
    @Override
    public List<CategoryVO> getCategoryList(Long parentId) {
        //查询所有配置过商品的类目
        List<PmsCategory> categoryList = this.list(
                new LambdaQueryWrapper<PmsCategory>()
                        //.eq(PmsCategory::getVisible, GlobalConstants.STATUS_YES)
                        .orderByDesc(PmsCategory::getSort)
        );
        List<CategoryVO> list = recursionTree(parentId != null ? parentId : 0l, categoryList);
        return list;
    }

    /**
     * 分类列表（级联）
     */
    @Override
    public List<Option> getCategoryOptions() {
        List<PmsCategory> categoryList = this.list(
                new LambdaQueryWrapper<PmsCategory>()
                        .eq(PmsCategory::getVisible, GlobalConstants.STATUS_YES)
                        .orderByAsc(PmsCategory::getSort)
        );
        List<Option> list = recursionzsl(0l, categoryList);
        return list;
    }

    private List<Option> recursionzsl(Long parentId, List<PmsCategory> categoryList) {
        List<Option> list = new ArrayList<>();
        Optional.ofNullable(categoryList)
                .ifPresent(categories ->
                        categories.stream().filter(category ->
                                        category.getParentId().equals(parentId))
                                .forEach(category -> {
                                    Option categoryVO = new Option<>(category.getId(), category.getName());
                                    BeanUtil.copyProperties(category, categoryVO);
                                    List<Option> children = recursionzsl(category.getId(), categoryList);
                                    categoryVO.setChildren(children);
                                    list.add(categoryVO);
                                })
                );
        return list;
    }


    /**
     * 新增/修改分类
     *
     * @return 分类ID
     */
    @Override
    public Long saveCategory(PmsCategory category) {
        this.saveOrUpdate(category);
        return category.getId();
    }


    /**
     * APP分类列表（有商品的类目）
     * 逻辑：从有商品的三级类目向上组装
     */
    @Override
    public List<CategoryVO> getAppCategoryList(Long parentId) {
        // 构建缓存Key
        String cacheKey = CATEGORY_CACHE_PREFIX + (parentId == null ? 0 : parentId);

        // 1. 先从缓存获取
        Object cachedResult = redisTemplate.opsForValue().get(cacheKey);
        if (cachedResult != null) {
            return (List<CategoryVO>) cachedResult;
        }

        // 2. 查询有商品的三级类目（通过Mapper SQL）
        List<PmsCategory> validCategories = categoryMapper.selectValidCategoriesWithProducts();

        // 3. 收集二级类目ID（从有效三级类目直接获取）
        Set<Long> l2Ids = validCategories.stream()
                .map(PmsCategory::getParentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 4. 查询二级类目
        List<PmsCategory> l2Categories = new ArrayList<>();
        Set<Long> l1Ids = new HashSet<>();
        if (!l2Ids.isEmpty()) {
            l2Categories = this.list(
                    new LambdaQueryWrapper<PmsCategory>()
                            .in(PmsCategory::getId, l2Ids)
            );
            l1Ids = l2Categories.stream()
                    .map(PmsCategory::getParentId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }

        // 5. 查询一级类目
        List<PmsCategory> allRelevantCategories = new ArrayList<>(validCategories);
        allRelevantCategories.addAll(l2Categories);
        if (!l1Ids.isEmpty()) {
            List<PmsCategory> l1Categories = this.list(
                    new LambdaQueryWrapper<PmsCategory>()
                            .in(PmsCategory::getId, l1Ids)
            );
            allRelevantCategories.addAll(l1Categories);
        }

        // 6. 构建映射表
        Map<Long, CategoryVO> nodeMap = allRelevantCategories.stream()
                .collect(Collectors.toMap(PmsCategory::getId, c -> {
                    CategoryVO vo = new CategoryVO();
                    BeanUtil.copyProperties(c, vo);
                    vo.setChildren(new ArrayList<>());
                    return vo;
                }));

        // 7. 向上组装树（直接通过ID查找，不需要再次遍历）
        for (PmsCategory l3Category : validCategories) {
            CategoryVO l3Node = nodeMap.get(l3Category.getId());
            if (l3Node == null) continue;

            // 二级父节点
            CategoryVO l2Node = nodeMap.get(l3Category.getParentId());
            if (l2Node == null) continue;

            // 添加三级到二级（去重）
            boolean l2HasL3 = l2Node.getChildren().stream()
                    .anyMatch(child -> child.getId().equals(l3Node.getId()));
            if (!l2HasL3) {
                l2Node.getChildren().add(l3Node);
            }

            // 一级父节点
            CategoryVO l1Node = nodeMap.get(l2Node.getParentId());
            if (l1Node == null) continue;

            // 添加二级到一级（去重）
            boolean l1HasL2 = l1Node.getChildren().stream()
                    .anyMatch(child -> child.getId().equals(l2Node.getId()));
            if (!l1HasL2) {
                l1Node.getChildren().add(l2Node);
            }
        }

        // 8. 返回结果
        List<CategoryVO> result;
        if (parentId == null || parentId == 0L) {
            // 一级类目
            result = nodeMap.values().stream()
                    .filter(vo -> vo.getLevel() == 1)
                    .filter(vo -> vo.getChildren() != null && !vo.getChildren().isEmpty())
                    .sorted(Comparator.comparingInt(CategoryVO::getSort))
                    .collect(Collectors.toList());
        } else {
            // 二级类目
            result = nodeMap.values().stream()
                    .filter(vo -> vo.getParentId() != null && vo.getParentId().equals(parentId))
                    .filter(vo -> vo.getChildren() != null && !vo.getChildren().isEmpty())
                    .sorted(Comparator.comparingInt(CategoryVO::getSort))
                    .collect(Collectors.toList());
        }

        // 9. 存入缓存（3分钟过期）
        if (!result.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, result, CACHE_EXPIRE_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
        }

        return result;
    }

}
