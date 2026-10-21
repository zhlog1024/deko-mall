package com.deko.mall.ums.controller.feign;

import com.deko.mall.common.result.Result;
import com.deko.mall.common.security.util.SecurityUtils;
import com.deko.mall.ums.api.MemberAssetsOperateFeignClient;
import com.deko.mall.ums.dto.AssetOperateRequest;
import com.deko.mall.ums.dto.MemberAssetsDTO;
import com.deko.mall.ums.service.UmsMemberAssetsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 会员资产 Feign Controller
 */
@Slf4j
@Tag(name = "会员资产接口")
@RestController
@RequestMapping("/feign-api/v1/asset")
@RequiredArgsConstructor
public class MemberAssetsOperateFeignController implements MemberAssetsOperateFeignClient {

    private final UmsMemberAssetsService umsMemberAssetsService;

    // 查询相关

    @Override
    @Operation(summary = "获取会员资产")
    @GetMapping("/get")
    public Result<MemberAssetsDTO> getMemberAsset(@RequestParam("assetCode") String assetCode) {
        Long memberId = SecurityUtils.getMemberId();
        return Result.success(umsMemberAssetsService.getMemberAsset(memberId, assetCode));
    }

    // 操作相关

    @Override
    @Operation(summary = "通用资产操作", description = "统一处理订单、转账、内部调整等场景")
    @PostMapping("/operate")
    public Result<Boolean> operateAsset(@RequestBody AssetOperateRequest request) {
        log.info("资产操作请求: operateType={}, memberId={}, assetCode={}, num={}, bizNo={}",
                request.getOperateType(), request.getMemberId(), request.getAssetCode(),
                request.getNum(), request.getBizNo());

        try {
            boolean success = umsMemberAssetsService.operate(request);
            return Result.success(success);
        } catch (Exception e) {
            log.error("资产操作失败: operateType={}, memberId={}, error={}",
                    request.getOperateType(), request.getMemberId(), e.getMessage(), e);
            return Result.failed("资产操作失败: " + e.getMessage());
        }
    }
}
