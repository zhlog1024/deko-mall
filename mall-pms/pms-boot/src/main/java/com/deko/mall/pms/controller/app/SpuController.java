package com.deko.mall.pms.controller.app;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import com.deko.mall.common.security.util.SecurityUtils;
import com.deko.mall.pms.enums.SpuTypeEnum;
import com.deko.mall.ums.api.FavorFeignClient;
import com.deko.mall.ums.api.GoodsBrowseFeignClient;
import com.deko.mall.pms.model.query.SpuPageQuery;
import com.deko.mall.pms.model.vo.*;
import com.deko.mall.pms.service.SpuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * APP端商品管理接口
 * 提供移动端商品相关的查询接口，包括：
 * 1. 商品列表查询
 * 2. 商品详情查询
 * 3. 秒杀商品查询
 */
@Tag(name = "App-商品接口", description = "提供APP端商品查询相关接口")
@RestController
@Slf4j
@RequestMapping("/app-api/v1/spu")
@RequiredArgsConstructor
public class SpuController {

    private final SpuService spuService;
    private final GoodsBrowseFeignClient goodsBrowseFeignClient;
    private final FavorFeignClient favorFeignClient;

    @Operation(summary = "商品分页列表（含筛选选项）", description = "获取商品分页列表，同时返回品牌/价格等筛选选项")
    @GetMapping("/pages")
    public Result<SearchResultVO> listPagedSpuForApp(
            @Parameter(description = "查询参数，支持按类目、品牌、关键词等条件筛选") SpuPageQuery queryParams) {
        SearchResultVO searchResult = spuService.listPagedSpuWithFilterOptions(queryParams);
        return Result.success(searchResult);
    }

    @Operation(summary = "获取商品详情", description = "获取指定商品的详细信息，包括商品属性、SKU等")
    @GetMapping("/{spuId}")
    public Result<SpuDetailVO> getSpuDetail(
            @Parameter(description = "商品ID") @PathVariable Long spuId) {
        SpuDetailVO spuDetailVO = spuService.getSpuDetailForApp(spuId);
        // 静默记录浏览历史 + 检查收藏状态
        try {
            Long memberId = SecurityUtils.getMemberId();
            if (memberId != null) {
                goodsBrowseFeignClient.recordBrowse(memberId, spuId);
                Result<Boolean> favorResult = favorFeignClient.isFavored(memberId, "GOODS", spuId);
                if (Result.isSuccess(favorResult) && spuDetailVO.getGoodsInfo() != null) {
                    spuDetailVO.getGoodsInfo().setIsFavor(favorResult.getData());
                }
            }
        } catch (Exception ignored) {
            // 未登录或调用失败，不影响商品详情返回
        }
        return Result.success(spuDetailVO);
    }

    @Operation(summary = "获取秒杀商品列表", description = "获取当前正在秒杀的商品列表")
    @GetMapping("/seckilling")
    public Result<List<SeckillingSpuVO>> listSeckillingSpu() {
        List<SeckillingSpuVO> list = spuService.listSeckillingSpu();
        return Result.success(list);
    }

    @Operation(summary = "普通商品分页列表", description = "获取普通商品的分页列表")
    @GetMapping("/normal/page")
    public PageResult listNormalSpu(
            @Parameter(description = "查询参数，支持按类目、品牌等条件筛选") SpuPageQuery queryParams) {
        queryParams.setSpuType(SpuTypeEnum.NORMAL.getValue());
        IPage<PmsSpuPageVO> result = spuService.listPagedSpu(queryParams);
        return PageResult.success(result);
    }

}
