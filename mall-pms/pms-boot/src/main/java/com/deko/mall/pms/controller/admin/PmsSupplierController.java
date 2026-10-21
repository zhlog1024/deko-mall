package com.deko.mall.pms.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import com.deko.mall.pms.model.form.SupplierAuditForm;
import com.deko.mall.pms.model.form.SupplierForm;
import com.deko.mall.pms.model.query.SupplierPageQuery;
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
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
public class PmsSupplierController {

    private final SupplierService supplierService;

    @Operation(summary = "供应商分页列表")
    @GetMapping("/page")
    public PageResult<SupplierVO> getSupplierPages(SupplierPageQuery queryParams) {
        IPage<SupplierVO> result = supplierService.getSupplierPages(queryParams);
        return PageResult.success(result);
    }

    @Operation(summary = "供应商详情")
    @GetMapping("/{id}")
    public Result<SupplierVO> getSupplier(
            @Parameter(description = "供应商ID") @PathVariable Long id
    ) {
        SupplierVO supplierVO = supplierService.getSupplierById(id);
        return Result.success(supplierVO);
    }

    @Operation(summary = "新增供应商")
    @PostMapping
    public Result<Boolean> addSupplier(@RequestBody SupplierForm supplierForm) {
        supplierForm.setPassword("123456");
        supplierForm.setConfirmPassword("123456");
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


    @Operation(summary = "审核供应商")
    @PutMapping("/{id}/audit")
    public Result<Boolean> auditSupplier(
            @Parameter(description = "供应商ID") @PathVariable Long id,
            @RequestBody SupplierAuditForm auditForm
    ) {
        boolean result = supplierService.auditSupplier(id, auditForm);
        return Result.judge(result);
    }
}
