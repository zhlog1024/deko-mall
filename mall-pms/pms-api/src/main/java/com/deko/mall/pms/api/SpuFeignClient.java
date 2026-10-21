package com.deko.mall.pms.api;

import com.deko.mall.common.result.Result;
import com.deko.mall.common.web.config.FeignDecoderConfig;
import com.deko.mall.pms.model.dto.PmsSpuDTO;
import com.deko.mall.pms.model.dto.SpuDynamicDataDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;


@FeignClient(value = "mall-pms", contextId = "spu", configuration = {FeignDecoderConfig.class})
public interface SpuFeignClient {

    @PostMapping("/feign-api/v1/spu/get/spu-list")
    Result<List<PmsSpuDTO>> getSpuList(@RequestBody List<Long> productIds);

    @PostMapping("/feign-api/v1/spu/get/spu-list-app")
    Result<List<PmsSpuDTO>> getSpuListForApp(@RequestBody List<Long> productIds);

    /**
     * 获取商品动态数据（价格、库存、销量等）
     *
     * @param spuIds SPU ID列表
     * @return 商品动态数据，key为"s"+spuId
     */
    @GetMapping("/feign-api/v1/spu/get/dynamic-data")
    Result<Map<String, SpuDynamicDataDTO>> getSpuDynamicData(
            @RequestParam(required = false) List<Long> spuIds);

    /**
     * 获取商品分页列表（用于装修器选择）
     *
     * @param params 查询参数 Map
     * @return 商品分页列表（Map格式）
     */
    @PostMapping("/feign-api/v1/spu/page")
    Result<Map<String, Object>> getSpuPage(@RequestBody Map<String, Object> params);

}
