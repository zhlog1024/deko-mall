package com.deko.mall.pms.controller.scm;

import com.deko.mall.common.result.Result;
import com.deko.mall.common.security.util.SecurityUtils;
import com.deko.mall.pms.model.dto.ShopUpdateDTO;
import com.deko.mall.pms.model.vo.ShopVO;
import com.deko.mall.pms.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 供应商后台-店铺控制层
 * <p>
 * 供应商可自助维护店铺名称/logo/长图/简介，避免审核通过后店铺信息为空。
 */
@Tag(name = "scm-店铺管理接口")
@RestController
@RequestMapping("/scm-api/v1/shop")
@RequiredArgsConstructor
public class ScmShopController {

    private final ShopService shopService;

    @Operation(summary = "获取我的店铺信息", description = "不存在时自动创建默认店铺")
    @GetMapping
    public Result<ShopVO> getMyShop() {
        Long supplierId = SecurityUtils.getSupplierId();
        return Result.success(shopService.getScmShopVO(supplierId));
    }

    @Operation(summary = "修改我的店铺信息", description = "仅可修改店铺名称/logo/长图/简介")
    @PutMapping
    public Result<Boolean> updateMyShop(@RequestBody ShopUpdateDTO dto) {
        Long supplierId = SecurityUtils.getSupplierId();
        if (dto.getShopName() == null || dto.getShopName().isBlank()) {
            return Result.failed("店铺名称不能为空");
        }
        boolean result = shopService.updateScmShopInfo(supplierId, dto);
        return Result.judge(result);
    }
}
