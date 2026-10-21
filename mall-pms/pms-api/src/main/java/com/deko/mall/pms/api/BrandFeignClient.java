package com.deko.mall.pms.api;

import com.deko.mall.common.result.Result;
import com.deko.mall.common.web.config.FeignDecoderConfig;
import com.deko.mall.pms.model.dto.PmsBrandDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 品牌 Feign 客户端
 */
@FeignClient(value = "mall-pms", contextId = "brand", configuration = {FeignDecoderConfig.class})
public interface BrandFeignClient {

    /**
     * 获取品牌列表（简单信息）
     *
     * @return 品牌列表
     */
    @GetMapping("/feign-api/v1/brands/simple")
    Result<List<PmsBrandDTO>> getBrandList();

    /**
     * 根据ID列表获取品牌
     *
     * @param brandIds 品牌ID列表
     * @return 品牌列表
     */
    @GetMapping("/feign-api/v1/brands")
    Result<List<PmsBrandDTO>> getBrandsByIds(@RequestParam List<Long> brandIds);
}

