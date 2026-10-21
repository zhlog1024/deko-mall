package com.deko.mall.pms.controller.scm;

import com.deko.mall.common.result.Result;
import com.deko.mall.common.security.util.SecurityUtils;
import com.deko.mall.pms.model.form.SupplierForm;
import com.deko.mall.pms.model.vo.SupplierVO;
import com.deko.mall.pms.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端-供应商控制层
 */
@Tag(name = "admin-供应商管理接口")
@RestController
@RequestMapping("/scm-api/v1/suppliers")
@RequiredArgsConstructor
public class ScmSupplierController {

    private final SupplierService supplierService;

    @Operation(summary = "供应商详情")
    @GetMapping
    public Result<SupplierVO> getSupplier() {
        Long supplierId = SecurityUtils.getSupplierId();
        SupplierVO supplierVO = supplierService.getSupplierById(supplierId);
        return Result.success(supplierVO);
    }

    @Operation(summary = "新增供应商")
    @PostMapping
    public Result<Boolean> addSupplier(@RequestBody SupplierForm supplierForm) {
        boolean result = supplierService.addSupplier(supplierForm);
        return Result.judge(result);
    }

    @Operation(summary = "修改供应商")
    @PutMapping("/{id}")
    public Result<Boolean> updateSupplier(
            @Parameter(description = "供应商ID") @PathVariable Long id,
            @RequestBody SupplierForm supplierForm
    ) {
        boolean result = supplierService.updateSupplier(id, supplierForm);
        return Result.judge(result);
    }
}
