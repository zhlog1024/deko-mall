package com.deko.mall.pms.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.common.base.IBaseEnum;
import com.deko.mall.pms.enums.SpuTypeEnum;
import com.deko.mall.pms.mapper.PmsSkuMapper;
import com.deko.mall.pms.mapper.PmsSpuMapper;
import com.deko.mall.pms.model.dto.SkuInfoDTO;
import com.deko.mall.pms.model.entity.PmsSku;
import com.deko.mall.pms.model.query.SpuPageQuery;
import com.deko.mall.pms.model.vo.SkuInfoVO;
import com.deko.mall.pms.service.SkuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品库存业务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SkuServiceImpl extends ServiceImpl<PmsSkuMapper, PmsSku> implements SkuService {

    private final PmsSkuMapper pmsSkuMapper;
    private final PmsSpuMapper spuMapper;


    /**
     * 获取商品库存信息
     *
     * @param skuId SKU ID
     * @return 商品库存信息
     */
    @Override
    public SkuInfoDTO getSkuInfo(Long skuId) {
        SkuInfoDTO sku = pmsSkuMapper.getSkuInfo(skuId);
        return sku;
    }

    /**
     * 获取商品库存信息列表
     *
     * @param skuIds SKU ID 列表
     * @return 商品库存信息列表
     */
    @Override
    public List<SkuInfoDTO> listSkuInfos(List<Long> skuIds) {
        return baseMapper.listSkuInfosWithSpu(skuIds);
    }

    @Override
    public IPage<SkuInfoVO> listPagedSku(SpuPageQuery queryParams) {
        Page<SkuInfoVO> page = new Page<>(queryParams.getPageNum(), queryParams.getPageSize());
        List<SkuInfoVO> list = pmsSkuMapper.listPagedSku(page, queryParams);
        list.forEach(item -> {
            if (item.getSpuType() != null) {
                item.setSpuTypeDesc(IBaseEnum.getLabelByValue(item.getSpuType(), SpuTypeEnum.class));
            }
        });
        page.setRecords(list);
        return page;
    }



    /** 关键字搜索的默认/最大条数：避免无界 LIKE 打爆 SKU 表 */
    private static final int SEARCH_DEFAULT_LIMIT = 200;

    private static final int SEARCH_MAX_LIMIT = 1000;

    @Override
    public List<Long> searchSkuIds(String keyword, Integer limit) {
        if (StrUtil.isBlank(keyword)) {
            return List.of();
        }
        int size = limit == null || limit <= 0 ? SEARCH_DEFAULT_LIMIT : Math.min(limit, SEARCH_MAX_LIMIT);
        return pmsSkuMapper.searchSkuIdsByNameOrSn(keyword.trim(), size);
    }
}
