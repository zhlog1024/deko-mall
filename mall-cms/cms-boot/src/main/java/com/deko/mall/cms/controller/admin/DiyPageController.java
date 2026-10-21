package com.deko.mall.cms.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.deko.mall.cms.model.dto.DiyPageHomeDTO;
import com.deko.mall.cms.model.dto.DiyPagePublishDTO;
import com.deko.mall.cms.model.dto.DiyPageSaveDTO;
import com.deko.mall.cms.model.query.DiyPageQuery;
import com.deko.mall.cms.model.vo.DiyPageVO;
import com.deko.mall.cms.service.DiyPageService;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 管理端DIY页面接口
 */
@Tag(name = "Admin-装修页面")
@RestController
@RequestMapping("/api/v1/diy-pages")
@Slf4j
@RequiredArgsConstructor
public class DiyPageController {

    private final DiyPageService diyPageService;

    @Operation(summary = "保存/更新页面")
    @PostMapping
    public Result<Long> save(@Valid @RequestBody DiyPageSaveDTO dto) {
        Long id = diyPageService.saveOrUpdatePage(dto);
        return Result.success(id);
    }

    @Operation(summary = "发布页面")
    @PostMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id, @Valid @RequestBody DiyPagePublishDTO dto) {
        dto.setId(id);
        diyPageService.publish(dto);
        return Result.success();
    }

    @Operation(summary = "设置为首页")
    @PutMapping("/{id}/home")
    public Result<Void> setHome(@PathVariable Long id, @RequestBody DiyPageHomeDTO dto) {
        dto.setId(id);
        diyPageService.setHome(dto);
        return Result.success();
    }

    @Operation(summary = "停用页面")
    @PostMapping("/{id}/disable")
    public Result<Void> disable(@PathVariable Long id) {
        diyPageService.disable(id);
        return Result.success();
    }

    @Operation(summary = "设置启用状态")
    @PutMapping("/{id}/enabled")
    public Result<Void> setEnabled(@PathVariable Long id, @RequestBody DiyPagePublishDTO dto) {
        diyPageService.setEnabled(id, dto.getIsEnabled());
        return Result.success();
    }

    @Operation(summary = "分页查询页面")
    @GetMapping("/pages")
    public PageResult<DiyPageVO> page(DiyPageQuery query) {
        IPage<DiyPageVO> result = diyPageService.page(query);
        return PageResult.success(result);
    }

    @Operation(summary = "页面详情")
    @GetMapping("/{id}")
    public Result<DiyPageVO> detail(@PathVariable Long id) {
        return Result.success(diyPageService.getDetail(id));
    }

    @Operation(summary = "首页DIY配置读取", description = "读取各客户端的首页装修配置，包含页面 ID 和生效时间")
    @GetMapping("/home-mode")
    public Result<Map<String, Object>> getHomeModeConfig() {
        return Result.success(diyPageService.getHomeModeConfig());
    }

    @Operation(summary = "首页DIY配置保存", description = "覆盖保存按端首页 DIY 配置（diy_id 必须存在）")
    @PutMapping("/home-mode")
    public Result<Void> saveHomeModeConfig(@RequestBody Map<String, Object> config) {
        diyPageService.saveHomeModeConfig(config);
        return Result.success();
    }

    @Operation(summary = "删除页面")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        diyPageService.delete(id);
        return Result.success();
    }

    @Operation(summary = "复制页面")
    @PostMapping("/{id}/copy")
    public Result<Long> copy(@PathVariable Long id) {
        return Result.success(diyPageService.copy(id));
    }
}

