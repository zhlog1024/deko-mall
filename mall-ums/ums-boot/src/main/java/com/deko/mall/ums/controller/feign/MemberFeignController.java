package com.deko.mall.ums.controller.feign;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.deko.mall.common.result.Result;
import com.deko.mall.ums.api.MemberFeignClient;
import com.deko.mall.ums.dto.*;
import com.deko.mall.ums.model.entity.UmsMember;
import com.deko.mall.ums.model.vo.MemberVO;
import com.deko.mall.ums.service.UmsMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "App-会员管理")
@RestController
@RequestMapping("/feign-api/v1/members")
@RequiredArgsConstructor
public class MemberFeignController implements MemberFeignClient {
    private final UmsMemberService memberService;
    private final UmsMemberService umsMemberService;


    @Operation(summary = "根据会员ID获取openid")
    @GetMapping("/get/openid/{id}")
    public Result<String> getMemberOpenId(@Parameter(name = "会员ID") @PathVariable Long id) {
        UmsMember member = memberService.getOne(new LambdaQueryWrapper<UmsMember>()
                .eq(UmsMember::getId, id)
                .select(UmsMember::getOpenid));
        String openid = member.getOpenid();
        return Result.success(openid);
    }

    @Operation(summary = "新增会员")
    @PostMapping
    public Result<Long> registerMember(@RequestBody MemberRegisterDTO member) {
        Long memberId = memberService.addMember(member);
        return Result.success(memberId);
    }

    @Operation(summary = "会员更新")
    @PostMapping("/update")
    public Result<Boolean> updateMember(@RequestBody MemberRegisterDTO member) {
        Boolean flag = memberService.updateMember(member);
        return Result.success(flag);
    }

    @Operation(summary = "注销会员")
    @PostMapping("/del")
    public Result<Boolean> delMember() {
        Boolean result = memberService.delMember();
        return Result.success(result);
    }

    @Operation(summary = "获取登录会员信息")
    @GetMapping("/me")
    public Result<MemberVO> getCurrMemberInfo() {
        MemberVO memberVO = memberService.getCurrMemberInfo();
        return Result.success(memberVO);
    }

    @Operation(summary = "根据手机号获取会员认证信息", hidden = true)
    @GetMapping("/mobile/{mobile}")
    public Result<MemberAuthDTO> loadUserByMobile(
            @Parameter(name = "手机号码") @PathVariable String mobile
    ) {
        MemberAuthDTO memberAuthInfo = memberService.getMemberByMobile(mobile);
        return Result.success(memberAuthInfo);
    }


    @Operation(summary = "根据微信参数获取用户手机号", hidden = true)
    @PostMapping("/mobile/wx/params")
    public Result<MemberAuthDTO> getWxUserInfo(@RequestBody WxAuthDTO wxAuthDTO) {
        MemberAuthDTO memberAuthInfo = memberService.getWxUserInfo(wxAuthDTO.getCode(), wxAuthDTO.getEncryptedData(), wxAuthDTO.getIv());
        return Result.success(memberAuthInfo);
    }

    @Operation(summary = "根据微信code获取openid", hidden = true)
    @GetMapping("/openid/wx/{code}")
    public Result<String> getOpenidByCode(
            @Parameter(name = "微信授权码") @PathVariable String code
    ) {
        String openid = memberService.getWxOpenidByCode(code);
        return Result.success(openid);
    }

    @Operation(summary = "根据手机号查看是否已注册")
    @GetMapping("/mobile-register/{mobile}")
    public Result<Boolean> getMemberByMobileRegister(
            @Parameter(name = "手机号码") @PathVariable String mobile
    ) {
        Boolean flag = memberService.getMemberByMobileRegistered(mobile);
        return Result.success(flag);
    }

    @Operation(summary = "获取会员地址列表")
    @GetMapping("/{memberId}/addresses")
    public Result<List<MemberAddressDTO>> listMemberAddresses(
            @Parameter(name = "会员ID") @PathVariable Long memberId
    ) {
        List<MemberAddressDTO> addresses = memberService.listMemberAddress(memberId);
        return Result.success(addresses);
    }

    @Operation(summary = "会员信息")
    @PostMapping("/info/{userId}")
    public Result<MemberInfoDTO> info(@PathVariable Long userId) {
        MemberInfoDTO memberInfoDTO = umsMemberService.getInfoByUserId(userId);
        return Result.success(memberInfoDTO);
    }

}
