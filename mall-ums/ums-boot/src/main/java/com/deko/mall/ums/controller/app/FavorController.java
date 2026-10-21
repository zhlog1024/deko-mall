package com.deko.mall.ums.controller.app;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.common.base.BasePageQuery;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import com.deko.mall.common.security.util.SecurityUtils;
import com.deko.mall.pms.api.ShopFeignClient;
import com.deko.mall.pms.api.SpuFeignClient;
import com.deko.mall.pms.model.dto.PmsSpuDTO;
import com.deko.mall.pms.model.dto.ShopDTO;
import com.deko.mall.ums.mapper.UmsFavorMapper;
import com.deko.mall.ums.model.entity.UmsFavor;
import com.deko.mall.ums.model.vo.FavorVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * App-会员统一收藏接口
 */
@Tag(name = "App-收藏")
@RestController
@RequestMapping("/app-api/v1/favor")
@RequiredArgsConstructor
public class FavorController extends ServiceImpl<UmsFavorMapper, UmsFavor> {

    private final SpuFeignClient spuFeignClient;
    private final ShopFeignClient shopFeignClient;

    @Operation(summary = "收藏列表")
    @PostMapping
    public PageResult<FavorVO> list(@RequestBody BasePageQuery query,
                                    @Parameter(description = "对象类型(GOODS-商品,SHOP-店铺)") String targetType) {
        Long memberId = SecurityUtils.getMemberId();
        IPage<UmsFavor> favorPage = this.page(new Page<>(query.getPageNum(), query.getPageSize()),
                new LambdaQueryWrapper<UmsFavor>()
                        .eq(UmsFavor::getMemberId, memberId)
                        .eq(StrUtil.isNotBlank(targetType), UmsFavor::getTargetType, targetType)
                        .eq(UmsFavor::getDeleted, 0)
                        .orderByDesc(UmsFavor::getCreateTime));

        List<UmsFavor> records = favorPage.getRecords();
        List<FavorVO> voList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(records)) {
            Map<String, List<Long>> targetIdMap = records.stream()
                    .collect(Collectors.groupingBy(UmsFavor::getTargetType,
                            Collectors.mapping(UmsFavor::getTargetId, Collectors.toList())));
            Map<Long, FavorVO> goodsMap = new HashMap<>();
            Map<Long, FavorVO> shopMap = new HashMap<>();
            for (Map.Entry<String, List<Long>> entry : targetIdMap.entrySet()) {
                if ("GOODS".equals(entry.getKey())) {
                    List<PmsSpuDTO> spuList = spuFeignClient.getSpuListForApp(entry.getValue()).getData();
                    if (spuList != null) {
                        for (PmsSpuDTO spu : spuList) {
                            FavorVO vo = new FavorVO();
                            vo.setTargetType("GOODS");
                            vo.setTargetId(spu.getId());
                            vo.setName(spu.getName());
                            vo.setPrice(spu.getPrice());
                            vo.setOriginalPrice(spu.getOriginalPrice());
                            vo.setPicUrl(spu.getPicUrl());
                            vo.setUnit(spu.getUnit());
                            goodsMap.put(spu.getId(), vo);
                        }
                    }
                } else if ("SHOP".equals(entry.getKey())) {
                    List<ShopDTO> shopList = shopFeignClient.getShopList(entry.getValue()).getData();
                    if (shopList != null) {
                        for (ShopDTO shop : shopList) {
                            FavorVO vo = new FavorVO();
                            vo.setTargetType("SHOP");
                            vo.setTargetId(shop.getId());
                            vo.setName(shop.getShopName());
                            vo.setLogo(shop.getLogo());
                            vo.setDescription(shop.getDescription());
                            shopMap.put(shop.getId(), vo);
                        }
                    }
                }
            }
            for (UmsFavor favor : records) {
                FavorVO vo;
                if ("GOODS".equals(favor.getTargetType())) {
                    vo = goodsMap.get(favor.getTargetId());
                } else if ("SHOP".equals(favor.getTargetType())) {
                    vo = shopMap.get(favor.getTargetId());
                } else {
                    vo = null;
                }
                if (vo == null) continue;
                vo.setFavorId(favor.getId());
                voList.add(vo);
            }
        }

        return PageResult.success(
                new Page<FavorVO>(favorPage.getCurrent(), favorPage.getSize(), favorPage.getTotal())
                        .setRecords(voList));
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping("/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        Long memberId = SecurityUtils.getMemberId();
        this.update(new LambdaUpdateWrapper<UmsFavor>()
                .eq(UmsFavor::getId, id)
                .eq(UmsFavor::getMemberId, memberId)
                .set(UmsFavor::getDeleted, 1));
        return Result.success();
    }

    @Operation(summary = "切换收藏")
    @PostMapping("/toggle/{targetType}/{targetId}")
    public Result<Boolean> toggle(@PathVariable String targetType, @PathVariable Long targetId) {
        Long memberId = SecurityUtils.getMemberId();
        // 查询不带 deleted 条件：记录可能存在（含已取消的软删记录），撞唯一键需复用
        UmsFavor exist = this.getOne(new LambdaQueryWrapper<UmsFavor>()
                .eq(UmsFavor::getMemberId, memberId)
                .eq(UmsFavor::getTargetType, targetType)
                .eq(UmsFavor::getTargetId, targetId)
                .last("LIMIT 1"));
        if (exist != null) {
            // 已收藏 → 取消（软删）
            if (exist.getDeleted() == null || exist.getDeleted() == 0) {
                exist.setDeleted(1);
                this.updateById(exist);
                return Result.success(false);
            }
            // 已取消 → 恢复收藏
            exist.setDeleted(0);
            this.updateById(exist);
            return Result.success(true);
        }
        UmsFavor favor = new UmsFavor();
        favor.setMemberId(memberId);
        favor.setTargetType(targetType);
        favor.setTargetId(targetId);
        this.save(favor);
        return Result.success(true);
    }
}
