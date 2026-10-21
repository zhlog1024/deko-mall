package com.deko.mall.cms.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.deko.mall.cms.model.dto.CustomViewSaveDTO;
import com.deko.mall.cms.model.query.CustomViewPageQuery;
import com.deko.mall.cms.model.vo.CustomViewDetailVO;
import com.deko.mall.cms.model.vo.CustomViewVO;
import com.deko.mall.cms.service.CustomViewService;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端-自定义页面接口
 */
@Tag(name = "admin-自定义页面接口")
@RestController
@RequestMapping("/api/v1/custom-view")
@RequiredArgsConstructor
public class CustomViewAdminController {

    private final CustomViewService customViewService;

    @Operation(summary = "自定义页面分页列表")
    @GetMapping("/page")
    public PageResult<CustomViewVO> pageCustomViews(CustomViewPageQuery query) {
        return PageResult.success(customViewService.pageCustomViews(query));
    }

    @Operation(summary = "自定义页面详情(编辑回显)")
    @GetMapping("/{id}")
    public Result<CustomViewDetailVO> getDetail(@Parameter(description = "页面ID") @PathVariable Long id) {
        return Result.success(customViewService.getDetail(id));
    }

    @Operation(summary = "新增自定义页面")
    @PostMapping
    public Result<Boolean> create(@Valid @RequestBody CustomViewSaveDTO dto) {
        return Result.judge(customViewService.saveCustomView(dto));
    }

    @Operation(summary = "编辑自定义页面", description = "支持局部字段更新(行内切换等)")
    @PutMapping("/{id}")
    public Result<Boolean> update(@Parameter(description = "页面ID") @PathVariable Long id,
                                  @RequestBody CustomViewSaveDTO dto) {
        dto.setId(id);
        return Result.judge(customViewService.updateCustomView(dto));
    }

    @Operation(summary = "删除自定义页面(逻辑删除)")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@Parameter(description = "页面ID") @PathVariable Long id) {
        return Result.judge(customViewService.deleteCustomView(id));
    }
}
