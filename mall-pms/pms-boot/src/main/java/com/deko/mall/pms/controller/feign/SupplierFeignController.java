package com.deko.mall.pms.controller.feign;

import com.deko.mall.common.result.Result;
import com.deko.mall.pms.api.SupplierFeignClient;
import com.deko.mall.pms.model.dto.SupplierDTO;
import com.deko.mall.pms.model.entity.PmsSupplier;
import com.deko.mall.pms.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端-供应商控制层
 */
@Tag(name = "feign-供应商feign接口")
@RestController
@RequestMapping("/feign-api/v1/suppliers")
@RequiredArgsConstructor
public class SupplierFeignController implements SupplierFeignClient {

    private final SupplierService supplierService;


    @Operation(summary = "供应商详情")
    @GetMapping("/userId/{id}")
    public Result<SupplierDTO> getSupplierByUserId(
            @Parameter(description = "用户ID") @PathVariable Long id
    ) {
        SupplierDTO supplierDTO = supplierService.getSupplierByUserId(id);
        return Result.success(supplierDTO);
    }

    @Operation(summary = "根据ID查询供应商")
    @GetMapping("/{id}")
    public Result<SupplierDTO> getById(
            @Parameter(description = "供应商ID") @PathVariable Long id
    ) {
        PmsSupplier supplier = supplierService.getById(id);
        if (supplier == null) return Result.failed("供应商不存在");
        SupplierDTO dto = new SupplierDTO();
        dto.setId(supplier.getId());
        dto.setSupplierName(supplier.getSupplierName());
        dto.setReturnContact(supplier.getReturnContact());
        dto.setReturnPhone(supplier.getReturnPhone());
        dto.setReturnAddress(supplier.getReturnAddress());
        return Result.success(dto);
    }
}
