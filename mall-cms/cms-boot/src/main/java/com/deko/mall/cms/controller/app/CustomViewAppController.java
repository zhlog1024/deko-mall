package com.deko.mall.cms.controller.app;

import com.deko.mall.cms.model.vo.CustomViewAppVO;
import com.deko.mall.cms.service.CustomViewService;
import com.deko.mall.common.result.Result;
import com.deko.mall.common.web.exception.BizException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * APP端-自定义页面接口
 */
@Tag(name = "App-自定义页面接口")
@RestController
@RequestMapping("/app-api/v1/custom-view")
@RequiredArgsConstructor
public class CustomViewAppController {

    private final CustomViewService customViewService;

    /** 官网/Web 404 兜底页 */
    private static final String NOT_FOUND_PAGE = "<!DOCTYPE html>\n"
            + "<html lang=\"zh-CN\">\n<head>\n<meta charset=\"utf-8\" />\n"
            + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n"
            + "<title>页面不存在</title>\n</head>\n"
            + "<body style=\"text-align:center;padding-top:80px;font-family:sans-serif\">\n"
            + "<h1>404</h1><p>页面不存在或已下架</p>\n</body>\n</html>\n";

    @Operation(summary = "自定义页面详情(启用且未删,访问量+1)")
    @GetMapping("/{id}")
    public Result<CustomViewAppVO> detail(@Parameter(description = "页面ID") @PathVariable Long id) {
        return Result.success(customViewService.getAppView(id));
    }

    /**
     * 官网/Web 端整页渲染
     * 直出完整 HTML 文档: SEO head + style(css) + body(html) + script(js);
     * 未启用/已删除 → HTTP 404 HTML 兜底页(供浏览器/搜索引擎识别)
     */
    @Operation(summary = "官网整页渲染HTML(启用且未删,访问量+1)")
    @GetMapping(value = "/{id}/web-page", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> webPage(@Parameter(description = "页面ID") @PathVariable Long id) {
        try {
            String html = customViewService.getWebPageHtml(id);
            return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
        } catch (BizException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.TEXT_HTML)
                    .body(NOT_FOUND_PAGE);
        }
    }
}
