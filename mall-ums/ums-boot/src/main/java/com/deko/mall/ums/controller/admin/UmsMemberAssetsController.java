package com.deko.mall.ums.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import com.deko.mall.common.security.util.SecurityUtils;
import com.deko.mall.ums.convert.MemberAssetConvert;
import com.deko.mall.ums.convert.MemberAssetsFlowConvert;
import com.deko.mall.ums.dto.AssetOperateRequest;
import com.deko.mall.ums.dto.MemberAssetsDTO;
import com.deko.mall.ums.dto.MemberAssetsVO;
import com.deko.mall.ums.enums.AssetOperateSceneEnum;
import com.deko.mall.ums.model.domain.UmsMemberAssetsFlow;
import com.deko.mall.ums.model.vo.UmsMemberAssetsFlowVO;
import com.deko.mall.ums.service.UmsMemberAssetsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Tag(name = "会员后台资产接口")
@RestController
@RequestMapping("/api/v1/asset")
@RequiredArgsConstructor
public class UmsMemberAssetsController {

    private final UmsMemberAssetsService umsMemberAssetsService;

    private final MemberAssetsFlowConvert memberAssetsFlowConvert;

    private final MemberAssetConvert memberAssetConvert;


    @Operation(summary = "增加会员积分")
    @PostMapping("/add")
    public Result<Boolean> addMemberAsset(
            @Parameter(description = "资产数量") @RequestParam BigDecimal num,
            @Parameter(description = "资产类型") @RequestParam String assetCode,
            @Parameter(description = "用户id") @RequestParam Long memberId) {
        Long operatorMemberId = SecurityUtils.getUserId();
        String uuid = UUID.randomUUID().toString();

        AssetOperateRequest request = new AssetOperateRequest();
        request.setOperateType(AssetOperateSceneEnum.ADJUST_IN);
        request.setMemberId(memberId);
        request.setAssetCode(assetCode);
        request.setNum(num);
        request.setBizNo(uuid);
        request.setAdjustReason("系统运营增加");

        boolean success = umsMemberAssetsService.operate(request);
        return Result.success(success);
    }

    @Operation(summary = "获取会员资产列表")
    @PostMapping("/list")
    public Result<List<MemberAssetsVO>> getMemberAssets(@Parameter(description = "用户id") @RequestParam Long memberId) {

        List<MemberAssetsDTO> memberAssets = umsMemberAssetsService.getMemberAssets(memberId);
        List<MemberAssetsVO> memberAssetsVOS = memberAssetConvert.dtoList2VoList(memberAssets);
        return Result.success(memberAssetsVOS);
    }

    @Operation(summary = "扣减会员积分")
    @PostMapping("/deduct")
    public Result<Boolean> deductMemberAsset(
            @Parameter(description = "资产数量") @RequestParam BigDecimal num,
            @Parameter(description = "资产类型") @RequestParam String assetCode,
            @Parameter(description = "用户id") @RequestParam Long memberId) {
        Long operatorMemberId = SecurityUtils.getUserId();
        String uuid = UUID.randomUUID().toString();

        AssetOperateRequest request = new AssetOperateRequest();
        request.setOperateType(AssetOperateSceneEnum.ADJUST_OUT);
        request.setMemberId(memberId);
        request.setAssetCode(assetCode);
        request.setNum(num);  // 正数，无需负号
        request.setBizNo(uuid);
        request.setAdjustReason("系统运营扣减");

        boolean success = umsMemberAssetsService.operate(request);
        return Result.success(success);
    }

    @Operation(summary = "积分流水")
    @PostMapping("/flow")
    public PageResult flowAsset(@Parameter(description = "页码") Long pageNum,
                                @Parameter(description = "每页数量") Long pageSize,
                                @Parameter(description = "类型") @RequestParam String assetCode,
                                @Parameter(description = "用户id") @RequestParam Long memberId,
                                @Parameter(description = "业务场景编码") @RequestParam(required = false) String sceneCode,
                                @Parameter(description = "业务单号") @RequestParam(required = false) String reqNo) {

        Page<UmsMemberAssetsFlow> assetsFlowPage = umsMemberAssetsService.flowAssetByAssetCode(memberId, assetCode, sceneCode, reqNo, new Page<>(pageNum, pageSize));
        Page<UmsMemberAssetsFlowVO> voPage = memberAssetsFlowConvert.toVoPage(assetsFlowPage);
        return PageResult.success(voPage);
    }

}
