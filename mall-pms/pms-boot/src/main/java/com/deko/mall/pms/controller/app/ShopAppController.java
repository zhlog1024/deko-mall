package com.deko.mall.pms.controller.app;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import com.deko.mall.pms.model.query.ShopPageQuery;
import com.deko.mall.pms.model.vo.ShopDetailVO;
import com.deko.mall.pms.model.vo.ShopVO;
import com.deko.mall.pms.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * APP端店铺接口
 */
@Tag(name = "App-店铺接口", description = "店铺广场、店铺详情")
@RestController
@Slf4j
@RequestMapping("/app-api/v1/shop")
@RequiredArgsConstructor
public class ShopAppController {

    private final ShopService shopService;

    @Operation(summary = "店铺广场分页列表", description = "获取店铺分页列表，可按关键字搜索")
    @GetMapping("/pages")
    public PageResult listShops(ShopPageQuery queryParams) {
        IPage<ShopVO> page = shopService.getShopPages(queryParams);
        return PageResult.success(page);
    }

    @Operation(summary = "店铺详情", description = "获取店铺信息及店铺商品分页列表")
    @GetMapping("/{shopId}")
    public Result<ShopDetailVO> getShopDetail(
            @Parameter(description = "店铺ID") @PathVariable Long shopId) {
        return Result.success(shopService.getShopDetail(shopId));
    }
}
