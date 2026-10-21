package com.deko.mall.pms.api;

import com.deko.mall.common.result.Result;
import com.deko.mall.common.web.config.FeignDecoderConfig;
import com.deko.mall.pms.model.dto.SupplierDTO;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(value = "mall-pms", contextId = "supplier", configuration = {FeignDecoderConfig.class})
public interface SupplierFeignClient {

    /**
     * 获取商品库存信息
     */
    @GetMapping("/feign-api/v1/suppliers/userId/{id}")
    Result<SupplierDTO> getSupplierByUserId(@Parameter(description = "用户ID") @PathVariable Long id);

    @GetMapping("/feign-api/v1/suppliers/{id}")
    Result<SupplierDTO> getById(@Parameter(description = "供应商ID") @PathVariable Long id);

}
