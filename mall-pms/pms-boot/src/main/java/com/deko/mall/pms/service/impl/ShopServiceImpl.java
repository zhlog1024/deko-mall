package com.deko.mall.pms.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.common.result.Result;
import com.deko.mall.common.web.exception.BizException;
import com.deko.mall.pms.config.PmsResultCode;
import com.deko.mall.pms.mapper.PmsShopMapper;
import com.deko.mall.pms.model.entity.PmsShop;
import com.deko.mall.pms.model.entity.PmsSpu;
import com.deko.mall.pms.model.dto.ShopUpdateDTO;
import com.deko.mall.pms.model.query.ShopPageQuery;
import com.deko.mall.pms.model.query.SpuPageQuery;
import com.deko.mall.pms.model.vo.ShopDetailVO;
import com.deko.mall.pms.model.vo.ShopVO;
import com.deko.mall.pms.model.vo.SpuPageVO;
import com.deko.mall.pms.service.ShopService;
import com.deko.mall.pms.service.SpuService;
import com.deko.mall.ums.api.FavorFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 店铺服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShopServiceImpl extends ServiceImpl<PmsShopMapper, PmsShop> implements ShopService {

    private final PmsShopMapper pmsShopMapper;
    private final SpuService spuService;
    private final FavorFeignClient favorFeignClient;

    @Override
    public IPage<ShopVO> getShopPages(ShopPageQuery queryParams) {
        Page<PmsShop> page = new Page<>(queryParams.getPageNum(), queryParams.getPageSize());
        LambdaQueryWrapper<PmsShop> wrapper = new LambdaQueryWrapper<PmsShop>()
                .eq(PmsShop::getStatus, 1)
                .eq(queryParams.getShopId() != null, PmsShop::getId, queryParams.getShopId())
                .like(StrUtil.isNotBlank(queryParams.getKeywords()), PmsShop::getShopName, queryParams.getKeywords())
                .eq(StrUtil.isNotBlank(queryParams.getOwnerType()), PmsShop::getOwnerType, queryParams.getOwnerType())
                .orderByAsc(PmsShop::getSort)
                .orderByDesc(PmsShop::getCreateTime);
        page = pmsShopMapper.selectPage(page, wrapper);

        Page<ShopVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        if (page.getRecords() != null && !page.getRecords().isEmpty()) {
            List<ShopVO> voList = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
            fillShopStatistics(voList);
            voPage.setRecords(voList);
        }
        return voPage;
    }

    @Override
    public ShopDetailVO getShopDetail(Long shopId) {
        PmsShop shop = getById(shopId);
        if (shop == null) {
            throw new BizException(PmsResultCode.SHOP_NOT_FOUND);
        }
        if (shop.getStatus() == null || shop.getStatus() != 1) {
            throw new BizException(PmsResultCode.SHOP_DISABLED);
        }

        ShopDetailVO detail = new ShopDetailVO();
        ShopVO shopVO = toVO(shop);
        fillShopStatistics(List.of(shopVO));
        // 当前会员是否已收藏该店铺
        try {
            Long memberId = com.deko.mall.common.security.util.SecurityUtils.getMemberId();
            if (memberId != null) {
                Result<Boolean> favorResult = favorFeignClient.isFavored(memberId, "SHOP", shopId);
                shopVO.setIsFavor(Result.isSuccess(favorResult) && Boolean.TRUE.equals(favorResult.getData()));
            }
        } catch (Exception e) {
            log.warn("查询店铺收藏状态失败, shopId={}, error={}", shopId, e.getMessage());
        }
        detail.setShop(shopVO);

        // 店铺商品分页（仅上架商品，按店铺ID过滤）
        SpuPageQuery queryParams = new SpuPageQuery();
        queryParams.setShopId(shopId);
        queryParams.setStatus(1);
        detail.setGoodsPage(spuService.listPagedSpuForApp(queryParams));
        return detail;
    }

    @Override
    public PmsShop getShopBySupplierId(Long supplierId) {
        PmsShop shop = pmsShopMapper.selectOne(new LambdaQueryWrapper<PmsShop>()
                .eq(PmsShop::getOwnerType, "SUPPLIER")
                .eq(PmsShop::getOwnerId, supplierId)
                .last("LIMIT 1"));
        if (shop == null) {
            log.info("供应商店铺不存在, 自动创建: supplierId={}", supplierId);
            shop = new PmsShop();
            shop.setOwnerType("SUPPLIER");
            shop.setOwnerId(supplierId);
            shop.setShopName("店铺" + supplierId);
            shop.setStatus(1);
            shop.setSort(0);
            try {
                save(shop);
            } catch (Exception e) {
                // 并发下唯一键冲突，重新查询
                shop = pmsShopMapper.selectOne(new LambdaQueryWrapper<PmsShop>()
                        .eq(PmsShop::getOwnerType, "SUPPLIER")
                        .eq(PmsShop::getOwnerId, supplierId)
                        .last("LIMIT 1"));
            }
        }
        return shop;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PmsShop createShop(String ownerType, Long ownerId, String shopName, String logo, String description) {
        PmsShop shop = new PmsShop();
        shop.setOwnerType(ownerType);
        shop.setOwnerId(ownerId);
        shop.setShopName(shopName);
        shop.setLogo(logo);
        shop.setDescription(description);
        shop.setStatus(1);
        shop.setSort(0);
        save(shop);
        log.info("店铺创建成功: shopId={}, ownerType={}, ownerId={}", shop.getId(), ownerType, ownerId);
        return shop;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateShopInfo(ShopUpdateDTO dto) {
        PmsShop shop = getById(dto.getShopId());
        if (shop == null) {
            throw new BizException(PmsResultCode.SHOP_NOT_FOUND);
        }
        PmsShop update = new PmsShop();
        update.setId(dto.getShopId());
        update.setShopName(dto.getShopName());
        update.setLogo(dto.getLogo());
        update.setLogoLong(dto.getLogoLong());
        update.setDescription(dto.getDescription());
        update.setSort(dto.getSort());
        update.setStatus(dto.getStatus());
        update.setScore(dto.getScore());
        return updateById(update);
    }

    @Override
    public ShopVO getScmShopVO(Long supplierId) {
        PmsShop shop = getShopBySupplierId(supplierId);
        ShopVO vo = toVO(shop);
        // 供应商后台不展示统计信息
        vo.setGoodsCount(null);
        vo.setFavorCount(null);
        vo.setIsFavor(null);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateScmShopInfo(Long supplierId, ShopUpdateDTO dto) {
        PmsShop shop = getShopBySupplierId(supplierId);
        // 归属校验：只能修改自己名下的店铺
        if (!"SUPPLIER".equals(shop.getOwnerType()) || !supplierId.equals(shop.getOwnerId())) {
            throw new BizException(PmsResultCode.SHOP_NOT_FOUND);
        }
        PmsShop update = new PmsShop();
        update.setId(shop.getId());
        update.setShopName(dto.getShopName());
        update.setLogo(dto.getLogo());
        update.setLogoLong(dto.getLogoLong());
        update.setDescription(dto.getDescription());
        return updateById(update);
    }

    private ShopVO toVO(PmsShop shop) {
        ShopVO vo = new ShopVO();
        vo.setId(shop.getId());
        vo.setOwnerType(shop.getOwnerType());
        vo.setOwnerId(shop.getOwnerId());
        vo.setShopName(shop.getShopName());
        vo.setLogo(shop.getLogo());
        vo.setLogoLong(shop.getLogoLong());
        vo.setDescription(shop.getDescription());
        vo.setStatus(shop.getStatus());
        vo.setSort(shop.getSort());
        vo.setScore(shop.getScore());
        vo.setCreateTime(shop.getCreateTime());
        return vo;
    }

    private void fillShopStatistics(List<ShopVO> voList) {
        for (ShopVO vo : voList) {
            // 商品数（该供应商下上架商品）
            Long goodsCount = spuService.lambdaQuery()
                    .eq(PmsSpu::getSupplierId, vo.getOwnerId())
                    .eq(PmsSpu::getStatus, 1)
                    .count();
            vo.setGoodsCount(goodsCount);
            // 收藏数（统一收藏中心）
            try {
                Result<Long> favorResult = favorFeignClient.countByTarget("SHOP", vo.getId());
                vo.setFavorCount(Result.isSuccess(favorResult) && favorResult.getData() != null ? favorResult.getData() : 0L);
            } catch (Exception e) {
                log.warn("查询店铺收藏数失败, shopId={}, error={}", vo.getId(), e.getMessage());
                vo.setFavorCount(0L);
            }
            vo.setIsFavor(false);
        }
    }
}
