package com.deko.mall.oms.controller.app;

import com.deko.mall.common.result.Result;
import com.deko.mall.oms.model.vo.CarrierVO;
import com.deko.mall.oms.model.vo.LogisticsDetailVO;
import com.deko.mall.oms.service.logistics.LogisticsInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端物流控制器
 */
@Tag(name = "用户端-物流查询")
@RestController
@RequiredArgsConstructor
@RequestMapping("/app-api/v1/logistics")
public class LogisticsController {

    private final LogisticsInfoService logisticsInfoService;

    @Operation(summary = "查询物流详情")
    @GetMapping
    public Result<LogisticsDetailVO> getLogisticsDetail(
            @Parameter(description = "物流单号") @RequestParam String trackingNo
    ) {
        LogisticsDetailVO detail = logisticsInfoService.queryLogisticsDetail(trackingNo);
        return Result.success(detail);
    }


    @Operation(summary = "获取物流公司列表")
    @GetMapping("/delivery/carriers")
    public Result<List<CarrierVO>> getCarrierList() {
        List<CarrierVO> carriers = logisticsInfoService.getCarrierList();
        return Result.success(carriers);
    }

}
