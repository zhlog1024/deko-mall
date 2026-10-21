package com.deko.mall.ums.controller.app;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import com.deko.mall.common.security.util.SecurityUtils;
import com.deko.mall.ums.convert.MemberAssetConvert;
import com.deko.mall.ums.convert.MemberAssetsFlowConvert;
import com.deko.mall.ums.dto.MemberAssetsDTO;
import com.deko.mall.ums.dto.MemberAssetsVO;
import com.deko.mall.ums.model.domain.UmsMemberAssetsFlow;
import com.deko.mall.ums.model.form.PointsTransferForm;
import com.deko.mall.ums.model.vo.UmsMemberAssetsFlowVO;
import com.deko.mall.ums.service.UmsMemberAssetsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "会员app资产接口")
@RestController
@RequestMapping("/app-api/v1/asset")
@RequiredArgsConstructor
public class MemberAssetsController {

    private final UmsMemberAssetsService umsMemberAssetsService;
    private final MemberAssetConvert memberAssetConvert;
    private final MemberAssetsFlowConvert memberAssetsFlowConvert;

    @GetMapping("/get")
    @Operation(summary = "获取会员资产")
    public Result<MemberAssetsVO> getMemberAsset(@RequestParam("assetCode") String assetCode) {
        Long memberId = SecurityUtils.getMemberId();
        MemberAssetsDTO memberAsset = umsMemberAssetsService.getMemberAsset(memberId, assetCode);
        MemberAssetsVO memberAssetsVO = memberAssetConvert.dto2Vo(memberAsset);
        return Result.success(memberAssetsVO);
    }

    @Operation(summary = "获取会员资产列表")
    @PostMapping("/list")
    public Result<List<MemberAssetsVO>> getMemberAssets() {
        Long memberId = SecurityUtils.getMemberId();
        List<MemberAssetsDTO> memberAssets = umsMemberAssetsService.getMemberAssets(memberId);
        List<MemberAssetsVO> memberAssetsVOS = memberAssetConvert.dtoList2VoList(memberAssets);
        return Result.success(memberAssetsVOS);
    }

    @Operation(summary = "积分流水")
    @PostMapping("/flow")
    public PageResult flowAsset(@Parameter(name = "页码") Long pageNum,
                                @Parameter(name = "每页数量") Long pageSize,
                                @Parameter(description = "资产id") @RequestParam Long id) {
        Long memberId = SecurityUtils.getMemberId();
        Page<UmsMemberAssetsFlow> assetsFlowIPage = umsMemberAssetsService.flowAsset(memberId, id, new Page<>(pageNum, pageSize));
        IPage<UmsMemberAssetsFlowVO> voPage = memberAssetsFlowConvert.toVoPage(assetsFlowIPage);
        return PageResult.success(voPage);
    }

    @Operation(summary = "积分转账")
    @PostMapping("/transfer")
    public Result transferPoints(@Validated @RequestBody PointsTransferForm form) {
        Long memberId = SecurityUtils.getMemberId();
        umsMemberAssetsService.transferAssets(memberId, form.getReceiverMobile(), form.getNum(), form.getRemark());
        return Result.success();
    }
}
