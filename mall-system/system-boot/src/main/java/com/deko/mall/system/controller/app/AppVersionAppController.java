package com.deko.mall.system.controller.app;

import com.deko.mall.common.result.Result;
import com.deko.mall.system.model.dto.AppVersionCheckDTO;
import com.deko.mall.system.model.dto.AppVersionCheckRequest;
import com.deko.mall.system.service.SysAppVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * APP端版本检测接口
 */
@Tag(name = "App-APP版本检测")
@RestController("appVersionAppController")
@RequestMapping("/app-api/v1/app-version")
@RequiredArgsConstructor
public class AppVersionAppController {

    private final SysAppVersionService appVersionService;

    @Operation(summary = "检测APP版本更新")
    @PostMapping("/check")
    public Result<AppVersionCheckDTO> check(@RequestBody AppVersionCheckRequest request) {
        return Result.success(appVersionService.checkUpdate(
                request.getAppVersion(), request.getPlatform()));
    }
}
