package com.deko.mall.pms.controller.feign;

import com.deko.mall.common.result.Result;
import com.deko.mall.pms.api.ShopFeignClient;
import com.deko.mall.pms.model.dto.ShopDTO;
import com.deko.mall.pms.model.entity.PmsShop;
import com.deko.mall.pms.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 店铺 Feign 接口实现
 */
@Tag(name = "feign-店铺feign接口")
@RestController
@RequestMapping("/feign-api/v1/shop")
@RequiredArgsConstructor
public class ShopFeignController implements ShopFeignClient {

    private final ShopService shopService;

    @Operation(summary = "批量获取店铺信息")
    @PostMapping("/get/list")
    public Result<List<ShopDTO>> getShopList(@RequestBody List<Long> shopIds) {
        List<ShopDTO> dtoList = new ArrayList<>();
        if (CollectionUtils.isEmpty(shopIds)) {
            return Result.success(dtoList);
        }
        List<PmsShop> shopList = shopService.listByIds(shopIds);
        if (shopList != null) {
            for (PmsShop shop : shopList) {
                ShopDTO dto = new ShopDTO();
                dto.setId(shop.getId());
                dto.setOwnerType(shop.getOwnerType());
                dto.setOwnerId(shop.getOwnerId());
                dto.setShopName(shop.getShopName());
                dto.setLogo(shop.getLogo());
                dto.setLogoLong(shop.getLogoLong());
                dto.setDescription(shop.getDescription());
                dto.setStatus(shop.getStatus());
                dto.setCreateTime(shop.getCreateTime());
                dtoList.add(dto);
            }
        }
        return Result.success(dtoList);
    }
}
