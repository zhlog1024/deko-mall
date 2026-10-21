package com.deko.mall.cms.controller.app;

import com.deko.mall.cms.model.vo.DiyPageAppVO;
import com.deko.mall.cms.service.DiyAppTabbarService;
import com.deko.mall.cms.service.DiyPageService;
import com.deko.mall.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * App端 - DIY 页面渲染接口
 * 用于前端页面渲染时获取已保存的DIY页面配置并填充实际数据
 */
@Tag(name = "App-DIY页面渲染")
@RestController
@RequestMapping("/app-api/v1/diy-pages")
@Slf4j
@RequiredArgsConstructor
public class PageAppController {

    private final DiyPageService diyPageService;
    private final DiyAppTabbarService diyAppTabbarService;

    @Operation(summary = "获取首页装修", description = "按客户端、启用状态和生效时间获取首页装修，并填充业务数据")
    @GetMapping("/home")
    public Result<DiyPageAppVO> home(
            @Parameter(description = "客户端类型：weixin/app/h5，缺省默认 weixin")
            @RequestParam(value = "application_client_type", required = false) String clientType) {
        return Result.success(diyPageService.getAppHome(clientType));
    }

    @Operation(summary = "获取 App 底部导航配置", description = "获取底部导航装修数据（business=home），供 App 端全局渲染，数据源 cms_diy_app_tabbar 本地表")
    @GetMapping("/app-tabbar")
    public Result<Object> appTabbar() {
        return Result.success(diyAppTabbarService.getTabbarConfig("home"));
    }

    @Operation(summary = "获取指定装修页面", description = "获取指定页面的装修配置（仅返回已启用页面），已填充实际数据")
    @GetMapping("/{id}")
    public Result<DiyPageAppVO> detail(@PathVariable Long id) {
        return Result.success(diyPageService.getAppDetail(id));
    }
}
