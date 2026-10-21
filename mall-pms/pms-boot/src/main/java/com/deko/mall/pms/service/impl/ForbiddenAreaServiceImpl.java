package com.deko.mall.pms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.common.security.util.SecurityUtils;
import com.deko.mall.pms.mapper.PmsForbiddenAreaMapper;
import com.deko.mall.pms.model.entity.PmsForbiddenArea;
import com.deko.mall.pms.model.form.ForbiddenAreaForm;
import com.deko.mall.pms.model.query.ForbiddenAreaQuery;
import com.deko.mall.pms.model.vo.ForbiddenAreaVO;
import com.deko.mall.pms.service.ForbiddenAreaService;
import com.deko.mall.pms.service.SpuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 禁售区域服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ForbiddenAreaServiceImpl extends ServiceImpl<PmsForbiddenAreaMapper, PmsForbiddenArea> implements ForbiddenAreaService {

    private final SpuService spuService;

    @Override
    public IPage<ForbiddenAreaVO> pageForbiddenAreas(ForbiddenAreaQuery query) {
        Long supplierId = query.getSupplierId();
        if (supplierId == null) {
            supplierId = SecurityUtils.getSupplierId();
        }

        LambdaQueryWrapper<PmsForbiddenArea> queryWrapper = new LambdaQueryWrapper<PmsForbiddenArea>()
                .eq(supplierId != null, PmsForbiddenArea::getSupplierId, supplierId)
                .like(StringUtils.hasText(query.getRegionNames()), PmsForbiddenArea::getRegionNames, query.getRegionNames())
                .like(StringUtils.hasText(query.getRegionCodes()), PmsForbiddenArea::getRegionCodes, query.getRegionCodes())
                .orderByDesc(PmsForbiddenArea::getCreateTime);

        Page<PmsForbiddenArea> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<PmsForbiddenArea> resultPage = this.page(page, queryWrapper);

        Page<ForbiddenAreaVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        List<ForbiddenAreaVO> voList = resultPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public ForbiddenAreaVO getForbiddenAreaDetail(Long id) {
        PmsForbiddenArea forbiddenArea = this.getById(id);
        Assert.notNull(forbiddenArea, "禁售区域不存在");

        return this.convertToVO(forbiddenArea);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createForbiddenArea(ForbiddenAreaForm form) {
        // 设置供应商ID
        if (form.getSupplierId() == null) {
            form.setSupplierId(SecurityUtils.getSupplierId());
        }

        // 保存禁售区域信息
        PmsForbiddenArea forbiddenArea = new PmsForbiddenArea();
        BeanUtils.copyProperties(form, forbiddenArea);

        return this.save(forbiddenArea);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateForbiddenArea(Long id, ForbiddenAreaForm form) {
        PmsForbiddenArea forbiddenArea = this.getById(id);
        Assert.notNull(forbiddenArea, "禁售区域不存在");

        // 更新禁售区域信息
        BeanUtils.copyProperties(form, forbiddenArea);

        return this.updateById(forbiddenArea);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteForbiddenAreas(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }

        return this.removeByIds(ids);
    }

    @Override
    public List<ForbiddenAreaVO> getForbiddenAreaList(Long supplierId) {
        LambdaQueryWrapper<PmsForbiddenArea> queryWrapper = new LambdaQueryWrapper<PmsForbiddenArea>()
                .eq(PmsForbiddenArea::getSupplierId, supplierId)
                .orderByDesc(PmsForbiddenArea::getUpdateTime);

        List<PmsForbiddenArea> forbiddenAreas = this.list(queryWrapper);

        return forbiddenAreas.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isInForbiddenArea(String regionCode, Long supplierId) {
        if (!StringUtils.hasText(regionCode) || supplierId == null) {
            return false;
        }

        // 查询供应商的所有禁售区域
        LambdaQueryWrapper<PmsForbiddenArea> queryWrapper = new LambdaQueryWrapper<PmsForbiddenArea>()
                .eq(PmsForbiddenArea::getSupplierId, supplierId);

        List<PmsForbiddenArea> forbiddenAreas = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(forbiddenAreas)) {
            return false;
        }

        // 判断区域是否在禁售区域内
        for (PmsForbiddenArea forbiddenArea : forbiddenAreas) {
            String regionCodes = forbiddenArea.getRegionCodes();
            if (StringUtils.hasText(regionCodes)) {
                List<String> codeList = Arrays.asList(regionCodes.split(","));
                if (codeList.contains(regionCode)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 转换为VO对象
     *
     * @param forbiddenArea 禁售区域实体
     * @return 禁售区域VO
     */
    private ForbiddenAreaVO convertToVO(PmsForbiddenArea forbiddenArea) {
        ForbiddenAreaVO vo = new ForbiddenAreaVO();
        BeanUtils.copyProperties(forbiddenArea, vo);
        return vo;
    }

    /**
     * 批量检查商品在指定区域是否禁售
     *
     * @param request 请求参数，包含商品SPU ID列表和地区信息
     * @return 禁售的商品SPU ID列表
     */
    @Override
    public List<Long> batchCheckForbiddenProducts(com.deko.mall.pms.api.ForbiddenAreaFeignClient.ForbiddenAreaCheckRequest request) {
        if (request == null || request.getSpuIds() == null || request.getSpuIds().isEmpty() ||
                request.getProvinceId() == null || request.getCityId() == null) {
            log.warn("禁售区域检查参数缺失");
            return new ArrayList<>();
        }

        try {
            // 构建区域编码
            String regionCode = String.format("%d_%d", request.getProvinceId(), request.getCityId());
            if (request.getAreaId() != null) {
                regionCode = regionCode + "," + request.getAreaId();
            }

            // 获取SPU与禁售区域的映射关系
            Map<Long, Long> spuForbiddenAreaMap = getSpuForbiddenAreaMap(request.getSpuIds());
            if (spuForbiddenAreaMap.isEmpty()) {
                log.info("没有找到商品的禁售区域关联");
                return new ArrayList<>();
            }

            List<Long> forbiddenSpuIds = new ArrayList<>();

            // 通过禁售区域ID查找包含指定区域的禁售区域
            for (Map.Entry<Long, Long> entry : spuForbiddenAreaMap.entrySet()) {
                Long spuId = entry.getKey();
                Long forbiddenAreaId = entry.getValue();

                if (forbiddenAreaId == null) {
                    continue;
                }

                // 获取禁售区域对象
                PmsForbiddenArea forbiddenArea = this.getById(forbiddenAreaId);
                if (forbiddenArea == null) {
                    continue;
                }

                // 检查区域编码是否在禁售区域的regionCodes中
                String regionCodes = forbiddenArea.getRegionCodes();
                if (StringUtils.hasText(regionCodes)) {
                    List<String> codeList = Arrays.asList(regionCodes.split(","));

                    // 拆分请求的区域编码，用于分层匹配
                    String[] parts = regionCode.split(",");
                    String provinceCode = parts[0]; // 省级编码

                    // 分层匹配：检查省级、市级、县级是否匹配
                    boolean isMatched = false;

                    // 1. 先检查维度最小的省级编码是否匹配
                    if (codeList.contains(provinceCode)) {
                        isMatched = true;
                    }

                    // 2. 如果有市级数据，检查省市组合是否匹配
                    if (!isMatched && parts.length > 1) {
                        String provinceCityCode = provinceCode + "," + parts[1];
                        if (codeList.contains(provinceCityCode)) {
                            isMatched = true;
                        }
                    }

                    // 3. 如果有县级数据，检查完整编码是否匹配
                    if (!isMatched && parts.length > 2) {
                        if (codeList.contains(regionCode)) {
                            isMatched = true;
                        }
                    }

                    if (isMatched) {
                        forbiddenSpuIds.add(spuId);
                        log.debug("商品{}(禁售区域{})在区域{}被禁售", spuId, forbiddenAreaId, regionCode);
                    }
                }
            }

            log.info("禁售商品ID列表: {}", forbiddenSpuIds);
            return forbiddenSpuIds;
        } catch (Exception e) {
            log.error("检查禁售区域异常", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取商品对应的禁售区域ID映射
     *
     * @param spuIds 商品ID列表
     * @return spuId与forbiddenAreaId的映射关系
     */
    private Map<Long, Long> getSpuForbiddenAreaMap(List<Long> spuIds) {
        Map<Long, Long> result = new HashMap<>();

        try {
            // 查询SPU列表
            List<com.deko.mall.pms.model.entity.PmsSpu> spus = spuService.listByIds(spuIds);
            if (spus != null && !spus.isEmpty()) {
                for (com.deko.mall.pms.model.entity.PmsSpu spu : spus) {
                    result.put(spu.getId(), spu.getForbiddenAreaId());
                }
            }
        } catch (Exception e) {
            log.error("获取商品禁售区域映射关系异常", e);
        }

        return result;
    }

    /**
     * 检查区域编码是否在指定的禁售区域列表中
     *
     * @param regionCode       区域编码（格式：省ID_市ID_区ID，如果某级ID为空则忽略）
     * @param forbiddenAreaIds 禁售区域ID列表
     * @return 包含该区域编码的禁售区域ID列表
     */
    @Override
    public List<Long> checkRegionInForbiddenAreas(String regionCode, List<Long> forbiddenAreaIds) {
        if (!StringUtils.hasText(regionCode) || CollectionUtils.isEmpty(forbiddenAreaIds)) {
            log.info("区域编码为空或禁售区域ID列表为空，无需检查");
            return new ArrayList<>();
        }

        log.info("开始检查区域编码[{}]是否在禁售区域列表{}中", regionCode, forbiddenAreaIds);
        List<Long> matchedAreaIds = new ArrayList<>();

        try {
            // 拆分请求的区域编码，用于分层匹配
            String[] parts = regionCode.split(",");
            if (parts.length == 0) {
                log.warn("无法从区域编码[{}]解析出有效的部分", regionCode);
                return matchedAreaIds;
            }

            // 构建不同层级的区域编码
            String provinceCode = parts[0]; // 省级编码
            String provinceCityCode = parts.length > 1 ? provinceCode + "," + parts[1] : null;
            String fullRegionCode = regionCode; // 完整区域编码

            log.debug("区域编码解析结果: 省级编码=[{}], 省市编码=[{}], 完整编码=[{}]",
                    provinceCode, provinceCityCode, fullRegionCode);

            // 批量获取指定的禁售区域记录
            List<PmsForbiddenArea> forbiddenAreas = this.listByIds(forbiddenAreaIds);
            if (CollectionUtils.isEmpty(forbiddenAreas)) {
                log.info("未找到任何指定的禁售区域记录，禁售区域ID列表：{}", forbiddenAreaIds);
                return matchedAreaIds;
            }

            // 为提高性能，将所有区域的区域编码预先解析并缓存在Map中
            Map<Long, Set<String>> areaRegionCodesMap = new HashMap<>();
            for (PmsForbiddenArea area : forbiddenAreas) {
                if (area != null && StringUtils.hasText(area.getRegionCodes())) {
                    String[] regionCodes = area.getRegionCodes().split(",");
                    Set<String> codeSet = new HashSet<>(Arrays.asList(regionCodes));
                    areaRegionCodesMap.put(area.getId(), codeSet);
                }
            }

            // 执行匹配逻辑
            for (PmsForbiddenArea area : forbiddenAreas) {
                if (area == null) {
                    continue;
                }

                Set<String> codeSet = areaRegionCodesMap.get(area.getId());
                if (codeSet == null || codeSet.isEmpty()) {
                    continue;
                }

                boolean isMatched = false;

                // 分层匹配，优先从最具体的编码开始匹配

                // 1. 先检查完整区域编码是否匹配（最精确的匹配）
                if (codeSet.contains(fullRegionCode)) {
                    isMatched = true;
                    log.debug("区域完整编码[{}]匹配禁售区域{}", fullRegionCode, area.getId());
                }

                // 2. 如果有市级数据，检查省市组合是否匹配
                if (!isMatched && provinceCityCode != null && codeSet.contains(provinceCityCode)) {
                    isMatched = true;
                    log.debug("区域省市编码[{}]匹配禁售区域{}", provinceCityCode, area.getId());
                }

                // 3. 最后检查省级编码是否匹配（最宽泛的匹配）
                if (!isMatched && codeSet.contains(provinceCode)) {
                    isMatched = true;
                    log.debug("区域省级编码[{}]匹配禁售区域{}", provinceCode, area.getId());
                }

                if (isMatched) {
                    matchedAreaIds.add(area.getId());
                    log.info("区域[{}]在禁售区域{}中", regionCode, area.getId());
                }
            }

            log.info("区域编码[{}]匹配到{}个禁售区域: {}", regionCode, matchedAreaIds.size(), matchedAreaIds);
            return matchedAreaIds;
        } catch (Exception e) {
            log.error("检查区域编码是否在禁售区域列表中发生异常: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}
