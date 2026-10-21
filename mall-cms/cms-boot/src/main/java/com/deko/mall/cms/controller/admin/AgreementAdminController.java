package com.deko.mall.cms.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.deko.mall.cms.model.dto.AgreementSaveDTO;
import com.deko.mall.cms.model.query.AgreementPageQuery;
import com.deko.mall.cms.model.vo.AgreementVO;
import com.deko.mall.cms.service.AgreementService;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端协议接口。
 */
@Tag(name = "admin-协议管理接口")
@RestController
@RequestMapping("/api/v1/agreement")
@RequiredArgsConstructor
public class AgreementAdminController {

    private final AgreementService agreementService;

    @Operation(summary = "协议分页列表")
    @GetMapping("/page")
    public PageResult<AgreementVO> pageAgreements(AgreementPageQuery query) {
        return PageResult.success(agreementService.pageAgreements(query));
    }

    @Operation(summary = "协议详情")
    @GetMapping("/{id}")
    public Result<AgreementVO> getDetail(@Parameter(description = "协议ID") @PathVariable Long id) {
        return Result.success(agreementService.getDetail(id));
    }

    @Operation(summary = "新增协议")
    @PostMapping
    @PreAuthorize("@ss.hasPerm('cms:agreement:add')")
    public Result<Boolean> create(@Valid @RequestBody AgreementSaveDTO dto) {
        return Result.judge(agreementService.saveAgreement(dto));
    }

    @Operation(summary = "编辑协议")
    @PutMapping("/{id}")
    @PreAuthorize("@ss.hasPerm('cms:agreement:edit')")
    public Result<Boolean> update(@Parameter(description = "协议ID") @PathVariable Long id,
                                  @Valid @RequestBody AgreementSaveDTO dto) {
        dto.setId(id);
        return Result.judge(agreementService.updateAgreement(dto));
    }

    @Operation(summary = "删除协议(逻辑删除)")
    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPerm('cms:agreement:delete')")
    public Result<Boolean> delete(@Parameter(description = "协议ID") @PathVariable Long id) {
        return Result.judge(agreementService.deleteAgreement(id));
    }
}
