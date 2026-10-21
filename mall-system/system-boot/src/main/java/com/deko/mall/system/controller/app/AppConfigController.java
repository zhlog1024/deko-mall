package com.deko.mall.system.controller.app;

import com.deko.mall.common.result.Result;
import com.deko.mall.system.service.AppConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * App端 - 全局配置初始化接口
 * base/common API，返回站点配置、协议URL等全局信息
 */
@Tag(name = "App-全局配置")
@RestController("appConfigController")
@RequestMapping("/app-api/v1/config")
@RequiredArgsConstructor
public class AppConfigController {

    private final AppConfigService appConfigService;

    @Operation(summary = "获取全局配置",
            description = "/base/common 接口。返回站点名称、协议URL、货币符号等全局配置。")
    @GetMapping("/common")
    public Result<Map<String, Object>> getCommonConfig() {
        return Result.success(appConfigService.getAppConfig());
    }
}
