package com.deko.mall.system.controller;

import com.deko.mall.common.result.Result;
import com.deko.mall.system.service.WechatMiniProgramService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 微信小程序控制器
 */
@Tag(name = "微信小程序管理")
@RestController
@RequestMapping("/app-api/v1/wechat")
@RequiredArgsConstructor
public class WechatMiniProgramController {

    private final WechatMiniProgramService wechatMiniProgramService;

    /**
     * 生成微信小程序 URL Link
     *
     * @param params 请求参数
     * @return URL Link
     */
    @Operation(summary = "生成微信小程序 URL Link")
    @PostMapping("/url-link/generate")
    public Result<Map<String, String>> generateUrlLink(@RequestBody Map<String, Object> params) {
        try {
            String path = (String) params.get("path");
            String scene = (String) params.get("scene");
            String envVersion = (String) params.get("env_version");
            Boolean isShorten = (Boolean) params.get("is_shorten");

            Map<String, String> result = wechatMiniProgramService.generateUrlLink(
                path, scene, envVersion, isShorten != null && isShorten
            );

            return Result.success(result);
        } catch (Exception e) {
            return Result.failed("生成小程序链接失败：" + e.getMessage());
        }
    }

    /**
     * 获取小程序二维码
     *
     * @param params 请求参数
     * @return 二维码图片的 base64 编码
     */
    @Operation(summary = "获取小程序二维码")
    @Parameter(name = "params", description = "请求参数")
    @PostMapping("/qrcode")
    public Result<Map<String, String>> getMiniProgramCode(@RequestBody Map<String, Object> params) {
        try {
            String scene = (String) params.get("scene");
            String page = (String) params.get("page");
            Boolean checkPath = (Boolean) params.get("check_path");
            String envVersion = (String) params.get("env_version");

            Map<String, String> result = wechatMiniProgramService.getMiniProgramCode(
                scene, page, checkPath != null && checkPath, envVersion
            );

            return Result.success(result);
        } catch (Exception e) {
            return Result.failed("获取小程序二维码失败：" + e.getMessage());
        }
    }

    /**
     * 获取分享配置（用于APP和H5分享小程序卡片）
     *
     * @param spuId 商品ID
     * @param shareId 分享人ID（可选）
     * @param page 小程序页面路径（可选，默认pages/product/product）
     * @return 分享配置信息
     */
    @Operation(summary = "获取分享配置")
    @GetMapping("/share-config")
    public Result<Map<String, Object>> getShareConfig(
            @Parameter(description = "商品ID") Long spuId,
            @Parameter(description = "分享人ID") @RequestParam(required = false) String shareId,
            @Parameter(description = "小程序页面路径") @RequestParam(required = false, defaultValue = "pages/product/product") String page) {
        try {
            Map<String, Object> result = wechatMiniProgramService.getShareConfig(spuId, shareId, page);
            return Result.success(result);
        } catch (Exception e) {
            return Result.failed("获取分享配置失败：" + e.getMessage());
        }
    }
}
