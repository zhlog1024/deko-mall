package com.deko.mall.pms.controller.feign;

import com.deko.mall.common.result.Result;
import com.deko.mall.pms.api.ForbiddenAreaFeignClient;
import com.deko.mall.pms.service.ForbiddenAreaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 禁售区域Feign接口实现
 */
@Tag(name = "feign-禁售区域服务")
@RestController
@RequestMapping("/feign-api/v1/forbidden-areas")
@RequiredArgsConstructor
@Slf4j
public class ForbiddenAreaFeignController implements ForbiddenAreaFeignClient {

    private final ForbiddenAreaService forbiddenAreaService;

    /**
     * 批量检查商品在指定区域是否禁售
     *
     * @param request 请求参数，包含商品SPU ID列表和地区信息
     * @return 禁售的商品SPU ID列表
     */
    @Operation(summary = "批量检查商品在指定区域是否禁售")
    @PostMapping("/batch-check")
    public Result<List<Long>> batchCheckForbiddenProducts(@RequestBody ForbiddenAreaFeignClient.ForbiddenAreaCheckRequest request) {
        log.info("批量检查商品是否禁售: {} {} {}", request.getProvinceId(), request.getCityId(), request.getAreaId());

        try {
            // 调用服务层进行禁售区域检查
            List<Long> forbiddenSpuIds = forbiddenAreaService.batchCheckForbiddenProducts(request);
            return Result.success(forbiddenSpuIds);
        } catch (Exception e) {
            log.error("检查禁售区域异常", e);
            return Result.failed("检查禁售区域异常：" + e.getMessage());
        }
    }

    /**
     * 检查区域编码是否在指定的禁售区域列表中
     *
     * @param regionCode       区域编码
     * @param forbiddenAreaIds 禁售区域ID列表
     * @return 包含该区域编码的禁售区域ID列表
     */
    @Operation(summary = "检查区域编码是否在指定禁售区域列表中")
    @GetMapping("/check-region")
    public Result<List<Long>> checkRegionInForbiddenAreas(
            @Parameter(description = "区域编码") @RequestParam("regionCode") String regionCode,
            @Parameter(description = "禁售区域ID列表") @RequestParam("forbiddenAreaIds") List<Long> forbiddenAreaIds) {

        log.info("检查区域编码是否在禁售区域列表中: 区域编码={}, 禁售区域列表={}",
                regionCode, forbiddenAreaIds);

        try {
            // 调用服务层检查区域编码是否在指定禁售区域列表中
            List<Long> matchedAreaIds = forbiddenAreaService.checkRegionInForbiddenAreas(regionCode, forbiddenAreaIds);
            return Result.success(matchedAreaIds);
        } catch (Exception e) {
            log.error("检查区域编码是否在禁售区域列表中异常", e);
            return Result.failed("检查区域编码是否在禁售区域列表中异常：" + e.getMessage());
        }
    }
}
