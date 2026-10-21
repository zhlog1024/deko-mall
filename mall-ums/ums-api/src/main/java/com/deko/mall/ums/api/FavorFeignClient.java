package com.deko.mall.ums.api;

import com.deko.mall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 会员统一收藏 Feign 接口
 */
@FeignClient(value = "mall-ums", contextId = "favor")
public interface FavorFeignClient {

    /**
     * 是否已收藏
     *
     * @param memberId   会员ID
     * @param targetType 对象类型(GOODS-商品, SHOP-店铺)
     * @param targetId   对象ID
     * @return 是否已收藏
     */
    @GetMapping("/feign-api/v1/favor/check")
    Result<Boolean> isFavored(@RequestParam("memberId") Long memberId,
                              @RequestParam("targetType") String targetType,
                              @RequestParam("targetId") Long targetId);

    /**
     * 对象收藏数
     *
     * @param targetType 对象类型(GOODS-商品, SHOP-店铺)
     * @param targetId   对象ID
     * @return 收藏数
     */
    @GetMapping("/feign-api/v1/favor/count")
    Result<Long> countByTarget(@RequestParam("targetType") String targetType,
                               @RequestParam("targetId") Long targetId);
}
