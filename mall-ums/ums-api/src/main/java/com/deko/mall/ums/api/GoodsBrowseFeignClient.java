package com.deko.mall.ums.api;

import com.deko.mall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "mall-ums", contextId = "goodsBrowse")
public interface GoodsBrowseFeignClient {

    @PostMapping("/feign-api/v1/goods-browse/record")
    Result<Void> recordBrowse(@RequestParam("memberId") Long memberId, @RequestParam("spuId") Long spuId);
}
