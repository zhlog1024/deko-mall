package com.deko.mall.ums.api;

import com.deko.mall.common.result.Result;
import com.deko.mall.ums.dto.AssetOperateRequest;
import com.deko.mall.ums.dto.MemberAssetsDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 会员资产 Feign Client
 * 职责分离：
 * - 查询：getMemberAsset
 * - 操作：operateAsset（统一处理订单、转账、内部调整等场景）
 */
@FeignClient(name = "mall-ums", contextId = "memberAssets")
@Validated
@Tag(name = "会员资产接口")
public interface MemberAssetsOperateFeignClient {

    /**
     * 查询会员资产
     *
     * @param assetCode 资产类型：POINT-积分
     * @return 资产信息
     */
    @Operation(summary = "获取会员资产", description = "根据资产类型查询会员的资产信息")
    @GetMapping("/feign-api/v1/asset/get")
    Result<MemberAssetsDTO> getMemberAsset(@Parameter(description = "资产类型") @RequestParam("assetCode") String assetCode);

    /**
     * 通用资产操作接口
     * 统一处理：订单、转账、内部调整等场景
     *
     * @param request 资产操作请求
     * @return 操作结果
     */
    @Operation(summary = "通用资产操作", description = "支持订单扣减/取消退回/退款退回、转账、内部调整")
    @PostMapping("/feign-api/v1/asset/operate")
    Result<Boolean> operateAsset(@RequestBody AssetOperateRequest request);
}
