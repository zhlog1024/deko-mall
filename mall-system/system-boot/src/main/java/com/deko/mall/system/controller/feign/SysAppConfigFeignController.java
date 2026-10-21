package com.deko.mall.system.controller.feign;

import com.deko.mall.common.result.Result;
import com.deko.mall.system.api.SysAppConfigFeignClient;
import com.deko.mall.system.dto.SysAppConfigValueDTO;
import com.deko.mall.system.service.SysAppConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * APP 配置（sys_app_config）Feign 控制器
 * 供 DIY 装修器等跨服务读写单条配置，写入后自动清理 APP_CONFIG 缓存
 */
@Tag(name = "Feign-APP配置")
@RestController
@RequestMapping("/feign-api/v1/system/app-config")
@RequiredArgsConstructor
public class SysAppConfigFeignController implements SysAppConfigFeignClient {

    private final SysAppConfigService sysAppConfigService;

    @Override
    @Operation(summary = "读取单条配置值", hidden = true)
    @GetMapping("/{configKey}")
    public Result<String> getConfigValue(@PathVariable("configKey") String configKey) {
        return Result.success(sysAppConfigService.getConfigValue(configKey));
    }

    @Override
    @Operation(summary = "写入单条配置值（清理APP_CONFIG缓存）", hidden = true)
    @PutMapping("/{configKey}")
    public Result<Boolean> saveConfigValue(@PathVariable("configKey") String configKey,
            @RequestBody SysAppConfigValueDTO dto) {
        String value = dto == null ? null : dto.getValue();
        return Result.success(sysAppConfigService.saveConfigValue(configKey, value));
    }
}
