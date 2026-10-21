package com.deko.mall.pms.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.common.web.exception.BizException;
import com.deko.mall.pms.config.PmsResultCode;
import com.deko.mall.pms.converter.PmsSpuConverter;
import com.deko.mall.pms.converter.PmsSpuGroupConverter;
import com.deko.mall.pms.enums.SpuTypeEnum;
import com.deko.mall.pms.mapper.PmsSpuGroupMapper;
import com.deko.mall.pms.mapper.PmsSpuGroupRelationMapper;
import com.deko.mall.pms.model.dto.PmsSpuDTO;
import com.deko.mall.pms.model.entity.PmsSpuGroup;
import com.deko.mall.pms.enums.SpuTypeEnum;
import com.deko.mall.pms.model.entity.PmsSpuGroupRelation;
import com.deko.mall.pms.model.form.SpuSortItem;
import com.deko.mall.pms.model.query.PmsSpuGroupQuery;
import com.deko.mall.pms.model.vo.PmsSpuGroupRenderVO;
import com.deko.mall.pms.model.vo.PmsSpuRenderVO;
import com.deko.mall.pms.model.vo.SpuPageVO;
import com.deko.mall.pms.model.vo.PmsSpuVO;
import com.deko.mall.pms.service.PmsSpuGroupService;
import com.deko.mall.pms.service.SpuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 商品分组服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PmsSpuGroupServiceImpl extends ServiceImpl<PmsSpuGroupMapper, PmsSpuGroup> implements PmsSpuGroupService {

    private static final String GROUP_PRODUCTS_CACHE_KEY = "product_group:products:";
    
    /**
     * 分组商品分页缓存Key前缀（用于装修组件）
     */
    private static final String GROUP_SPU_PAGE_CACHE_KEY = "diy:group_spu_page:";
    
    /**
     * 分组商品分页缓存过期时间（秒）- 5分钟
     */
    private static final long GROUP_SPU_PAGE_CACHE_EXPIRE_SECONDS = 5 * 60;
    
    private final PmsSpuGroupConverter pmsSpuGroupConverter;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PmsSpuGroupRelationMapper goodsGroupRelationMapper;
    private final PmsSpuConverter pmsSpuConverter;
    private final SpuService spuService;

    @Override
    public IPage<PmsSpuGroupRenderVO> listSpuGroupPages(PmsSpuGroupQuery queryParams) {
        LambdaQueryWrapper<PmsSpuGroup> queryWrapper = new LambdaQueryWrapper<PmsSpuGroup>()
                .like(StringUtils.hasText(queryParams.getName()), PmsSpuGroup::getName, queryParams.getName())
                .eq(Objects.nonNull(queryParams.getStatus()), PmsSpuGroup::getStatus, queryParams.getStatus())
                .orderByAsc(PmsSpuGroup::getSort);
        Page<PmsSpuGroup> page = new Page<>(queryParams.getPageNum(), queryParams.getPageSize());
        IPage<PmsSpuGroupRenderVO> result = this.page(page, queryWrapper).convert(pmsSpuGroupConverter::entity2VO);
        fillSpuCount(result.getRecords());
        return result;
    }

    @Override
    public List<PmsSpuGroupRenderVO> listEnabledSpuGroups() {
        LambdaQueryWrapper<PmsSpuGroup> queryWrapper = new LambdaQueryWrapper<PmsSpuGroup>()
                .eq(PmsSpuGroup::getStatus, 1)
                .orderByAsc(PmsSpuGroup::getSort)
                .orderByDesc(PmsSpuGroup::getCreateTime);
        List<PmsSpuGroup> groups = this.list(queryWrapper);
        List<PmsSpuGroupRenderVO> result = groups.stream()
                .map(pmsSpuGroupConverter::entity2VO)
                .collect(Collectors.toList());
        fillSpuCount(result);
        return result;
    }

    @Override
    public PmsSpuGroupRenderVO getSpuGroupDetail(Long id) {
        PmsSpuGroup goodsGroup = this.getById(id);
        if (goodsGroup == null) {
            return null;
        }
        PmsSpuGroupRenderVO vo = pmsSpuGroupConverter.entity2VO(goodsGroup);
        fillSpuCount(Collections.singletonList(vo));
        return vo;
    }

    private void fillSpuCount(List<PmsSpuGroupRenderVO> voList) {
        if (voList == null || voList.isEmpty()) {
            return;
        }
        List<Long> groupIds = voList.stream().map(PmsSpuGroupRenderVO::getId).collect(Collectors.toList());
        LambdaQueryWrapper<PmsSpuGroupRelation> countWrapper = new LambdaQueryWrapper<PmsSpuGroupRelation>()
                .in(PmsSpuGroupRelation::getGroupId, groupIds);
        List<PmsSpuGroupRelation> relations = goodsGroupRelationMapper.selectList(countWrapper);
        Map<Long, Long> countMap = relations.stream()
                .collect(Collectors.groupingBy(PmsSpuGroupRelation::getGroupId, Collectors.counting()));
        voList.forEach(vo -> vo.setSpuCount(countMap.getOrDefault(vo.getId(), 0L).intValue()));
    }

    private void checkNameUnique(String name, Long excludeId) {
        if (!StringUtils.hasText(name)) {
            return;
        }
        LambdaQueryWrapper<PmsSpuGroup> queryWrapper = new LambdaQueryWrapper<PmsSpuGroup>()
                .eq(PmsSpuGroup::getName, name)
                .ne(excludeId != null, PmsSpuGroup::getId, excludeId);
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BizException(PmsResultCode.GROUP_NAME_EXISTS);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createSpuGroup(PmsSpuGroup goodsGroup) {
        // 检查名称唯一性
        checkNameUnique(goodsGroup.getName(), null);

        // 设置默认值
        if (goodsGroup.getSort() == null) {
            goodsGroup.setSort(0);
        }

        // 默认启用状态
        goodsGroup.setStatus(1);

        if (goodsGroup.getIsSystem() == null) {
            goodsGroup.setIsSystem(0);
        }

        return this.save(goodsGroup);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSpuGroup(PmsSpuGroup goodsGroup) {
        // 检查名称唯一性
        checkNameUnique(goodsGroup.getName(), goodsGroup.getId());

        // 检查是否系统内置
        PmsSpuGroup existingGroup = this.getById(goodsGroup.getId());
        if (existingGroup != null && existingGroup.getIsSystem() != null && existingGroup.getIsSystem() == 1) {
            // 系统内置分组只允许修改名称、描述、图标和状态
            existingGroup.setName(goodsGroup.getName());
            existingGroup.setDescription(goodsGroup.getDescription());
            existingGroup.setIconUrl(goodsGroup.getIconUrl());
            existingGroup.setStatus(goodsGroup.getStatus());

            return this.updateById(existingGroup);
        }

        return this.updateById(goodsGroup);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSpuGroup(Long id) {
        // 检查是否系统内置
        PmsSpuGroup existingGroup = this.getById(id);
        if (existingGroup != null && existingGroup.getIsSystem() != null && existingGroup.getIsSystem() == 1) {
            throw new BizException(PmsResultCode.GROUP_SYSTEM_CANNOT_DELETE);
        }

        // 删除分组及关联关系
        boolean result = this.removeById(id);
        if (result) {
            // 删除分组与商品的关联关系
            LambdaQueryWrapper<PmsSpuGroupRelation> queryWrapper = new LambdaQueryWrapper<PmsSpuGroupRelation>()
                    .eq(PmsSpuGroupRelation::getGroupId, id);
            goodsGroupRelationMapper.delete(queryWrapper);
        }

        return result;
    }

    @Override
    public List<Long> getGroupSpuIds(Long groupId) {
        if (groupId == null) {
            return new ArrayList<>();
        }
        return goodsGroupRelationMapper.getSpuIdsByGroupId(groupId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addSpusToGroup(Long groupId, List<Long> spuIds) {
        // 1. 检查分组是否存在
        PmsSpuGroup group = this.getById(groupId);
        if (group == null) {
            throw new BizException(PmsResultCode.GROUP_NOT_FOUND);
        }

        // 2. 如果spuIds为空，则无需处理
        if (spuIds == null || spuIds.isEmpty()) {
            return true;
        }

        // 3. 查询已存在于分组中的商品ID
        LambdaQueryWrapper<PmsSpuGroupRelation> queryWrapper = new LambdaQueryWrapper<PmsSpuGroupRelation>()
                .eq(PmsSpuGroupRelation::getGroupId, groupId)
                .in(PmsSpuGroupRelation::getSpuId, spuIds);
        List<PmsSpuGroupRelation> existingRelations = goodsGroupRelationMapper.selectList(queryWrapper);
        Set<Long> existingSpuIds = existingRelations.stream()
                .map(PmsSpuGroupRelation::getSpuId)
                .collect(Collectors.toSet());

        // 4. 过滤出需要新增的商品ID
        List<Long> newSpuIds = spuIds.stream()
                .filter(spuId -> !existingSpuIds.contains(spuId))
                .collect(Collectors.toList());

        // 5. 查询当前分组中最大的排序值
        Integer maxSort = 0;
        LambdaQueryWrapper<PmsSpuGroupRelation> maxSortQuery = new LambdaQueryWrapper<PmsSpuGroupRelation>()
                .eq(PmsSpuGroupRelation::getGroupId, groupId)
                .orderByDesc(PmsSpuGroupRelation::getSort)
                .last("LIMIT 1");
        PmsSpuGroupRelation maxSortRelation = goodsGroupRelationMapper.selectOne(maxSortQuery);
        if (maxSortRelation != null && maxSortRelation.getSort() != null) {
            maxSort = maxSortRelation.getSort() + 1; // 从最大排序值之后开始
        }

        // 6. 批量添加新的关联，并设置递增的排序值
        if (!newSpuIds.isEmpty()) {
            for (int i = 0; i < newSpuIds.size(); i++) {
                PmsSpuGroupRelation relation = new PmsSpuGroupRelation();
                relation.setGroupId(groupId);
                relation.setSpuId(newSpuIds.get(i));
                relation.setSort(maxSort + i); // 使用递增的排序值
                goodsGroupRelationMapper.insert(relation);
            }
        }

        // 7. 清除缓存
        String cacheKey = GROUP_PRODUCTS_CACHE_KEY + groupId;
        redisTemplate.delete(cacheKey);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeSpusFromGroup(Long groupId, List<Long> spuIds) {
        if (groupId == null || spuIds == null || spuIds.isEmpty()) {
            return false;
        }

        LambdaQueryWrapper<PmsSpuGroupRelation> queryWrapper = new LambdaQueryWrapper<PmsSpuGroupRelation>()
                .eq(PmsSpuGroupRelation::getGroupId, groupId)
                .in(PmsSpuGroupRelation::getSpuId, spuIds);

        int count = goodsGroupRelationMapper.delete(queryWrapper);
        return count > 0;
    }

    @Override
    public IPage<PmsSpuRenderVO> pageGroupSpu(PmsSpuGroupQuery query) {
        if (query == null || query.getGroupId() == null) {
            log.warn("商品分组ID为空，无法获取分组商品");
            return new Page<>(1, 10);
        }

        Page<PmsSpuRenderVO> page = new Page<>(query.getPageNum(), query.getPageSize());

        // MyBatis-Plus 自动处理分页和总数
        IPage<PmsSpuRenderVO> result = goodsGroupRelationMapper.pageGroupSpuRenderVO(page, query.getGroupId());

        // 过滤下架商品
        result.setRecords(result.getRecords().stream()
                .filter(p -> p.getIsOnSale() != null && p.getIsOnSale())
                .collect(Collectors.toList()));

        return result;
    }

    @Override
    public IPage<SpuPageVO> pageGroupSpuForSpuPageVO(PmsSpuGroupQuery query) {
        if (query == null || query.getGroupId() == null) {
            log.warn("商品分组ID为空，无法获取分组商品");
            return new Page<>(1, 10);
        }

        // 1. 解析用户角色
        String roleType = resolveRoleType();

        // 2. 构建缓存Key（含角色）
        String cacheKey = buildGroupSpuCacheKey(query.getGroupId(), query.getPageNum(), query.getPageSize(), roleType);

        // 3. 尝试从缓存获取
        @SuppressWarnings("unchecked")
        IPage<SpuPageVO> cached = (IPage<SpuPageVO>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("命中分组商品缓存, groupId: {}, roleType: {}", query.getGroupId(), roleType);
            return cached;
        }

        // 4. 缓存未命中，查询数据库
        log.debug("分组商品缓存未命中，从数据库加载, groupId: {}", query.getGroupId());
        Page<SpuPageVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<SpuPageVO> result = goodsGroupRelationMapper.pageGroupSpuForSpuPageVO(page, query.getGroupId());

        // 5. 角色定价封装 → 写入缓存
        if (result != null && !result.getRecords().isEmpty()) {
            applySubscriptText(result.getRecords());
            applyRoleBasedPricing(result.getRecords(), roleType);
            redisTemplate.opsForValue().set(cacheKey, result, GROUP_SPU_PAGE_CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        }

        return result;
    }

    @Override
    public List<PmsSpuVO> getGroupSpuDetails(Long groupId) {
        // 1. 获取分组中的商品ID列表
        if (groupId == null) {
            return Collections.emptyList();
        }

        List<Long> spuIds = getGroupSpuIds(groupId);
        if (spuIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 调用商品服务获取详细信息
        List<PmsSpuDTO> data = spuService.getProductList(spuIds);

        // 3. 转换并按照 spuIds 的原始顺序排序返回
        List<PmsSpuVO> pmsSpuVOS = pmsSpuConverter.dto2VOList(data);

        // 按原始 spuIds 顺序排序
        Map<Long, PmsSpuVO> spuMap = pmsSpuVOS.stream()
                .collect(Collectors.toMap(PmsSpuVO::getId, Function.identity()));
        List<PmsSpuVO> sortedResult = spuIds.stream()
                .map(spuMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return sortedResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createSpuGroupWithSpus(PmsSpuGroup spuGroup, List<SpuSortItem> spuList) {
        // 1. 检查名称唯一性
        checkNameUnique(spuGroup.getName(), null);

        // 2. 设置默认值
        if (spuGroup.getSort() == null) {
            spuGroup.setSort(0);
        }
        spuGroup.setStatus(1);
        if (spuGroup.getIsSystem() == null) {
            spuGroup.setIsSystem(0);
        }

        // 3. 保存分组
        boolean saved = this.save(spuGroup);
        if (!saved) {
            return false;
        }

        // 4. 替换商品列表（保存排序）
        if (spuList != null) {
            replaceGroupSpus(spuGroup.getId(), spuList);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSpuGroupWithSpus(PmsSpuGroup spuGroup, List<SpuSortItem> spuList) {
        // 1. 检查名称唯一性
        checkNameUnique(spuGroup.getName(), spuGroup.getId());

        // 2. 检查是否系统内置
        PmsSpuGroup existingGroup = this.getById(spuGroup.getId());
        if (existingGroup != null && existingGroup.getIsSystem() != null && existingGroup.getIsSystem() == 1) {
            existingGroup.setName(spuGroup.getName());
            existingGroup.setDescription(spuGroup.getDescription());
            existingGroup.setIconUrl(spuGroup.getIconUrl());
            existingGroup.setStatus(spuGroup.getStatus());

            boolean updated = this.updateById(existingGroup);
            if (updated) {
                if (spuList != null) {
                    replaceGroupSpus(spuGroup.getId(), spuList);
                }
            }
            return updated;
        }

        // 3. 更新分组
        boolean updated = this.updateById(spuGroup);
        if (updated) {
            if (spuList != null) {
                replaceGroupSpus(spuGroup.getId(), spuList);
            }
        }

        return updated;
    }

    /**
     * 替换分组中的商品列表（用于保存排序）
     *
     * @param groupId 分组ID
     * @param spuList 商品列表（含排序）
     */
    private void replaceGroupSpus(Long groupId, List<SpuSortItem> spuList) {
        // 1. 删除旧的关联关系
        LambdaQueryWrapper<PmsSpuGroupRelation> deleteWrapper = new LambdaQueryWrapper<PmsSpuGroupRelation>()
                .eq(PmsSpuGroupRelation::getGroupId, groupId);
        goodsGroupRelationMapper.delete(deleteWrapper);

        // 2. 批量插入新的关联关系
        if (spuList != null && !spuList.isEmpty()) {
            for (SpuSortItem item : spuList) {
                PmsSpuGroupRelation relation = new PmsSpuGroupRelation();
                relation.setGroupId(groupId);
                relation.setSpuId(item.getSpuId());
                relation.setSort(item.getSort() != null ? item.getSort() : 0);
                goodsGroupRelationMapper.insert(relation);
            }
        }

        // 3. 清除缓存
        String cacheKey = GROUP_PRODUCTS_CACHE_KEY + groupId;
        redisTemplate.delete(cacheKey);
        // 清除分页缓存（所有页）
        clearGroupSpuPageCache(groupId);
    }
    
    /**
     * 构建分组商品分页缓存Key
     */
    private String buildGroupSpuCacheKey(Long groupId, Integer pageNum, Integer pageSize, String roleType) {
        return GROUP_SPU_PAGE_CACHE_KEY + groupId + ":" + pageNum + ":" + pageSize + ":" + roleType;
    }

    /**
     * 解析当前用户角色类型（用于缓存key）。
     * 统一按普通会员缓存。
     */
    private String resolveRoleType() {
        return "normal";
    }

    /**
     * 无角色价分层，不生成「会员减X元」角标。
     */
    private void applySubscriptText(List<SpuPageVO> list) {
        // no-op
    }

    /**
     * 统一按零售价展示
     */
    private void applyRoleBasedPricing(List<SpuPageVO> list, String roleType) {
        return;
    }
    
    /**
     * 清除指定分组的所有分页缓存
     */
    private void clearGroupSpuPageCache(Long groupId) {
        if (groupId == null) {
            return;
        }
        // 使用模式匹配删除所有相关缓存
        String pattern = GROUP_SPU_PAGE_CACHE_KEY + groupId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.debug("清除分组商品分页缓存, groupId: {}, 清除数量: {}", groupId, keys.size());
        }
    }
}

