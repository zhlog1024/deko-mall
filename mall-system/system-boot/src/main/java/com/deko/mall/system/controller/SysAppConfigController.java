package com.deko.mall.system.controller;

import com.deko.mall.common.result.Result;
import com.deko.mall.system.model.vo.AppConfigGroupVO;
import com.deko.mall.system.service.SysAppConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "09.APP配置管理")
@RestController
@RequestMapping("/api/v1/app-config")
@RequiredArgsConstructor
public class SysAppConfigController {

    private final SysAppConfigService sysAppConfigService;

    @Operation(summary = "获取APP配置（按分组）")
    @GetMapping
    public Result<List<AppConfigGroupVO>> getConfigs() {
        return Result.success(sysAppConfigService.getGroupedConfigs());
    }

    @Operation(summary = "批量保存APP配置")
    @PutMapping
    public Result<Boolean> saveConfigs(@RequestBody Map<String, Object> configMap) {
        return Result.success(sysAppConfigService.saveConfigs(configMap));
    }
}
