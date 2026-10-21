package com.deko.mall.pms.controller.feign;

import com.deko.mall.common.result.Result;
import com.deko.mall.pms.api.FreightFeignClient;
import com.deko.mall.pms.model.dto.FreightCalcParam;
import com.deko.mall.pms.model.dto.FreightTemplateDTO;
import com.deko.mall.pms.service.FreightTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 运费服务Feign接口实现
 */
@Tag(name = "APP-运费服务")
@RestController
@RequestMapping("/feign-api/v1/freight")
@RequiredArgsConstructor
@Slf4j
public class FreightFeignController implements FreightFeignClient {

    private final FreightTemplateService freightTemplateService;

    /**
     * 根据ID集合批量获取运费模板
     *
     * @param ids 运费模板ID集合
     * @return 运费模板列表
     */
    @Operation(summary = "根据ID集合批量获取运费模板")
    @GetMapping("/templates/batch")
    public Result<List<FreightTemplateDTO>> getFreightTemplatesByIds(
            @Parameter(description = "运费模板ID集合") @RequestParam("ids") List<Long> ids) {
        log.info("批量获取运费模板，ids: {}", ids);
        List<FreightTemplateDTO> templates = freightTemplateService.getFreightTemplatesByIds(ids);
        return Result.success(templates);
    }

    /**
     * 计算运费
     *
     * @param param 运费计算参数
     * @return 运费金额(单位 ： 元)
     */
    @Operation(summary = "计算运费")
    @PostMapping("/calculate")
    public Result<BigDecimal> calculateFreight(@RequestBody FreightCalcParam param) {
        log.info("计算运费，参数: {}", param);

        // 参数校验
        if (param == null || param.getTemplateId() == null) {
            log.warn("运费计算参数缺失");
            return Result.failed("运费计算参数缺失");
        }

        try {
            // 调用服务层计算运费
            BigDecimal freight = freightTemplateService.calculateFreight(param);
            return Result.success(freight);
        } catch (Exception e) {
            log.error("计算运费异常", e);
            return Result.failed("计算运费异常：" + e.getMessage());
        }
    }

    /**
     * 批量计算运费
     *
     * @param paramMap 运费模板ID与运费计算参数的映射
     * @return 运费模板ID与运费金额的映射(单位 ： 元)
     */
    @Operation(summary = "批量计算运费")
    @PostMapping("/batch-calculate")
    public Result<Map<Long, BigDecimal>> batchCalculateFreight(@RequestBody Map<Long, FreightCalcParam> paramMap) {
        log.info("批量计算运费，参数数量: {}", paramMap == null ? 0 : paramMap.size());

        // 参数校验
        if (CollectionUtils.isEmpty(paramMap)) {
            log.warn("批量运费计算参数为空");
            return Result.success(Collections.emptyMap());
        }

        try {
            // 结果映射
            Map<Long, BigDecimal> resultMap = new HashMap<>();

            // 为每个运费模板计算运费
            for (Map.Entry<Long, FreightCalcParam> entry : paramMap.entrySet()) {
                Long templateId = entry.getKey();
                FreightCalcParam param = entry.getValue();

                if (templateId == null || param == null) {
                    log.warn("运费计算参数无效，模板ID: {}", templateId);
                    continue;
                }

                // 确保参数中的模板ID与键一致
                param.setTemplateId(templateId);

                // 计算运费
                try {
                    BigDecimal freight = freightTemplateService.calculateFreight(param);
                    resultMap.put(templateId, freight);
                    log.debug("运费模板 {} 计算运费结果: {}", templateId, freight);
                } catch (Exception e) {
                    log.error("计算运费模板 {} 的运费时发生异常: {}", templateId, e.getMessage());
                    // 单个模板计算失败不影响其他模板的计算
                    resultMap.put(templateId, BigDecimal.ZERO);
                }
            }

            log.info("批量计算运费完成，共计算 {} 个模板的运费", resultMap.size());
            return Result.success(resultMap);
        } catch (Exception e) {
            log.error("批量计算运费异常", e);
            return Result.failed("批量计算运费异常：" + e.getMessage());
        }
    }
}
