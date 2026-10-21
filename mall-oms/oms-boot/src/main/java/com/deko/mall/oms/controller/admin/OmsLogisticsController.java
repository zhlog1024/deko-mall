package com.deko.mall.oms.controller.admin;

import com.deko.mall.common.result.Result;
import com.deko.mall.oms.enums.LogisticsTypeEnum;
import com.deko.mall.oms.model.dto.LogisticsDTO;
import com.deko.mall.oms.model.vo.CarrierVO;
import com.deko.mall.oms.model.vo.LogisticsDetailVO;
import com.deko.mall.oms.service.logistics.LogisticsInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 运营端物流控制器
 */
@Tag(name = "运营端-物流管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/logistics")
public class OmsLogisticsController {

    private final LogisticsInfoService logisticsInfoService;

    @Operation(summary = "订阅物流轨迹")
    @PostMapping("/subscribe")
    public Result<Boolean> subscribe(
            @Parameter(description = "订单号")
            @RequestParam String businessNo,
            @Parameter(description = "物流单号")
            @RequestParam String trackingNo,
            @Parameter(description = "物流公司编码")
            @RequestParam String carrierCode) {
        boolean success = logisticsInfoService.subscribeLogistics(LogisticsDTO.builder()
                .bizNo(businessNo)
                .logisticsType(LogisticsTypeEnum.DELIVERY.getValue())
                .carrierCode(carrierCode)
                .trackingNo(trackingNo)
                .build());
        return Result.success(success);
    }

    @Operation(summary = "查询物流详情")
    @GetMapping("/detail")
    public Result<LogisticsDetailVO> getLogisticsDetail(
            @Parameter(description = "物流单号") @RequestParam String trackingNo

    ) {
        LogisticsDetailVO detail = logisticsInfoService.queryLogisticsDetail(trackingNo);
        return Result.success(detail);
    }

    @Operation(summary = "主动查询物流轨迹 运维接口")
    @PostMapping("/query")
    public Result<Boolean> queryTrack(
            @Parameter(description = "物流单号")
            @RequestParam String trackingNo) {
        boolean success = logisticsInfoService.compensateQueryTrack(trackingNo);
        return Result.success(success);
    }


    @Operation(summary = "获取物流公司列表")
    @GetMapping("/delivery/carriers")
    public Result<List<CarrierVO>> getCarrierList() {
        List<CarrierVO> carriers = logisticsInfoService.getCarrierList();
        return Result.success(carriers);
    }

}
