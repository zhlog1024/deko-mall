package com.deko.mall.pms.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import com.deko.mall.pms.model.dto.ShopUpdateDTO;
import com.deko.mall.pms.model.query.ShopPageQuery;
import com.deko.mall.pms.model.vo.ShopVO;
import com.deko.mall.pms.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端-店铺控制层
 */
@Tag(name = "admin-店铺管理接口")
@RestController
@RequestMapping("/api/v1/shops")
@RequiredArgsConstructor
public class PmsShopController {

    private final ShopService shopService;

    @Operation(summary = "店铺分页列表")
    @GetMapping("/page")
    public PageResult<ShopVO> getShopPages(ShopPageQuery queryParams) {
        IPage<ShopVO> result = shopService.getShopPages(queryParams);
        return PageResult.success(result);
    }

    @Operation(summary = "店铺详情")
    @GetMapping("/{id}")
    public Result<ShopVO> getShop(
            @Parameter(description = "店铺ID") @PathVariable Long id) {
        ShopVO vo = new ShopVO();
        vo.setId(id);
        com.deko.mall.pms.model.entity.PmsShop shop = shopService.getById(id);
        if (shop == null) {
            return Result.failed("店铺不存在");
        }
        vo.setOwnerType(shop.getOwnerType());
        vo.setOwnerId(shop.getOwnerId());
        vo.setShopName(shop.getShopName());
        vo.setLogo(shop.getLogo());
        vo.setLogoLong(shop.getLogoLong());
        vo.setDescription(shop.getDescription());
        vo.setStatus(shop.getStatus());
        vo.setSort(shop.getSort());
        vo.setScore(shop.getScore());
        return Result.success(vo);
    }

    @Operation(summary = "修改店铺信息（logo/简介/名称/排序/状态/评分）")
    @PutMapping("/{id}")
    public Result<Boolean> updateShop(
            @Parameter(description = "店铺ID") @PathVariable Long id,
            @RequestParam(required = false) String shopName,
            @RequestParam(required = false) String logo,
            @RequestParam(required = false) String logoLong,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Integer sort,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) java.math.BigDecimal score) {
        ShopUpdateDTO dto = new ShopUpdateDTO();
        dto.setShopId(id);
        dto.setShopName(shopName);
        dto.setLogo(logo);
        dto.setLogoLong(logoLong);
        dto.setDescription(description);
        dto.setSort(sort);
        dto.setStatus(status);
        dto.setScore(score);
        boolean result = shopService.updateShopInfo(dto);
        return Result.judge(result);
    }
}
