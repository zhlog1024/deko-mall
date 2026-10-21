package com.deko.mall.pms.api;

import com.deko.mall.common.result.Result;
import com.deko.mall.common.web.config.FeignDecoderConfig;
import com.deko.mall.pms.model.dto.ShopDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 店铺 Feign 接口（供收藏中心等跨服务回查店铺信息）
 */
@FeignClient(value = "mall-pms", contextId = "shop", configuration = {FeignDecoderConfig.class})
public interface ShopFeignClient {

    /**
     * 批量获取店铺信息
     *
     * @param shopIds 店铺ID列表
     * @return 店铺信息列表
     */
    @PostMapping("/feign-api/v1/shop/get/list")
    Result<List<ShopDTO>> getShopList(@RequestBody List<Long> shopIds);
}
