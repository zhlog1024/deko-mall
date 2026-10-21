package com.deko.mall.ums.controller.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.common.base.BasePageQuery;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import com.deko.mall.common.security.util.SecurityUtils;
import com.deko.mall.pms.api.SpuFeignClient;
import com.deko.mall.pms.model.dto.PmsSpuDTO;
import com.deko.mall.ums.mapper.UmsGoodsBrowseMapper;
import com.deko.mall.ums.model.entity.UmsGoodsBrowse;
import com.deko.mall.ums.model.vo.GoodsBrowseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(name = "App-浏览历史")
@RestController
@RequestMapping("/app-api/v1/goods-browse")
@RequiredArgsConstructor
public class GoodsBrowseController extends ServiceImpl<UmsGoodsBrowseMapper, UmsGoodsBrowse> {

    private final SpuFeignClient spuFeignClient;

    @Operation(summary = "浏览历史列表")
    @PostMapping
    public PageResult<GoodsBrowseVO> list(@RequestBody BasePageQuery query) {
        Long memberId = SecurityUtils.getMemberId();
        IPage<UmsGoodsBrowse> browsePage = this.page(new Page<>(query.getPageNum(), query.getPageSize()),
                new LambdaQueryWrapper<UmsGoodsBrowse>()
                        .eq(UmsGoodsBrowse::getMemberId, memberId)
                        .eq(UmsGoodsBrowse::getDeleted, 0)
                        .orderByDesc(UmsGoodsBrowse::getCreateTime));

        // 查询 SPU 信息
        List<Long> spuIds = browsePage.getRecords().stream().map(UmsGoodsBrowse::getSpuId).toList();
        Map<Long, PmsSpuDTO> spuMap = new HashMap<>();
        if (!spuIds.isEmpty()) {
            List<PmsSpuDTO> spuList = spuFeignClient.getSpuListForApp(spuIds).getData();
            if (spuList != null) {
                for (PmsSpuDTO spu : spuList) {
                    spuMap.put(spu.getId(), spu);
                }
            }
        }

        // 组装 VO，带上浏览记录 id
        List<GoodsBrowseVO> voList = new ArrayList<>();
        for (UmsGoodsBrowse browse : browsePage.getRecords()) {
            PmsSpuDTO spu = spuMap.get(browse.getSpuId());
            if (spu == null) continue;
            GoodsBrowseVO vo = new GoodsBrowseVO();
            // 复制 SPU 字段
            vo.setId(spu.getId());
            vo.setName(spu.getName());
            vo.setPrice(spu.getPrice());
            vo.setOriginalPrice(spu.getOriginalPrice());
            vo.setPicUrl(spu.getPicUrl());
            vo.setUnit(spu.getUnit());
            vo.setBrowseId(browse.getId());
            voList.add(vo);
        }

        return PageResult.success(
                new Page<GoodsBrowseVO>(browsePage.getCurrent(), browsePage.getSize(), browsePage.getTotal())
                        .setRecords(voList));
    }

    @Operation(summary = "删除浏览记录")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long memberId = SecurityUtils.getMemberId();
        this.update(new LambdaUpdateWrapper<UmsGoodsBrowse>()
                .eq(UmsGoodsBrowse::getId, id)
                .eq(UmsGoodsBrowse::getMemberId, memberId)
                .set(UmsGoodsBrowse::getDeleted, 1));
        return Result.success();
    }

    @Operation(summary = "清空浏览历史")
    @DeleteMapping("/clear")
    public Result<Void> clear() {
        Long memberId = SecurityUtils.getMemberId();
        this.update(new LambdaUpdateWrapper<UmsGoodsBrowse>()
                .eq(UmsGoodsBrowse::getMemberId, memberId)
                .set(UmsGoodsBrowse::getDeleted, 1));
        return Result.success();
    }

    @Operation(summary = "记录浏览")
    @PostMapping("/record")
    public Result<Void> record(@RequestParam Long spuId) {
        Long memberId = SecurityUtils.getMemberId();
        this.update(new LambdaUpdateWrapper<UmsGoodsBrowse>()
                .eq(UmsGoodsBrowse::getMemberId, memberId)
                .eq(UmsGoodsBrowse::getSpuId, spuId)
                .set(UmsGoodsBrowse::getDeleted, 1));
        UmsGoodsBrowse browse = new UmsGoodsBrowse();
        browse.setMemberId(memberId);
        browse.setSpuId(spuId);
        this.save(browse);
        return Result.success();
    }
}
