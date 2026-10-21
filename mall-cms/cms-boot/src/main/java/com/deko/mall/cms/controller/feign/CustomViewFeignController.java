package com.deko.mall.cms.controller.feign;

import com.deko.mall.cms.api.dto.CustomViewItemDTO;
import com.deko.mall.cms.service.CustomViewService;
import com.deko.mall.common.result.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 自定义页面内部 Feign 接口（供装修桥接、官网等）
 */
@Tag(name = "feign-自定义页面内部接口")
@RestController
@RequestMapping("/feign-api/v1/custom-views")
@RequiredArgsConstructor
public class CustomViewFeignController {

    private final CustomViewService customViewService;

    @GetMapping("/enabled-list")
    public Result<List<CustomViewItemDTO>> listEnabled() {
        return Result.success(customViewService.listEnabledFeign());
    }
}
