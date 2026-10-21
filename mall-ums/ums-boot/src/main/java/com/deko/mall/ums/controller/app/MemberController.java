package com.deko.mall.ums.controller.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.deko.mall.common.result.Result;
import com.deko.mall.common.security.util.SecurityUtils;
import com.deko.mall.ums.config.MemberCacheConfig;
import com.deko.mall.ums.dto.MemberAddressDTO;
import com.deko.mall.ums.dto.MemberAuthDTO;
import com.deko.mall.ums.dto.MemberRegisterDTO;
import com.deko.mall.ums.model.entity.UmsMember;
import com.deko.mall.ums.model.vo.*;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.deko.mall.common.constant.RedisConstants;
import com.deko.mall.ums.service.UmsMemberAssetsFlowService;
import com.deko.mall.ums.service.UmsMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Tag(name = "App-会员管理")
@RestController
@RequestMapping("/app-api/v1/members")
@RequiredArgsConstructor
public class MemberController {
    private static final String MEMBER_INFO_CACHE_PREFIX = "member:info:";

    private final UmsMemberService memberService;
    private final UmsMemberAssetsFlowService assetFlowService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MemberCacheConfig memberCacheConfig;

    @Operation(summary = "根据会员ID获取openid")
    @GetMapping("/get/openid/{id}")
    public Result<String> getMemberById(@Parameter(name = "会员ID") @PathVariable Long id) {
        UmsMember member = memberService.getOne(new LambdaQueryWrapper<UmsMember>()
                .eq(UmsMember::getId, id)
                .select(UmsMember::getOpenid));
        String openid = member.getOpenid();
        return Result.success(openid);
    }

    @Operation(summary = "会员注册")
    @PostMapping("/register")
    public Result<Long> registerMember(@RequestBody MemberRegisterDTO member) {
        Long memberId = memberService.registerMember(member);
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
        Long memberId = SecurityUtils.getMemberId();
        String cacheKey = MEMBER_INFO_CACHE_PREFIX + memberId;

        MemberVO memberVO = (MemberVO) redisTemplate.opsForValue().get(cacheKey);
        if (memberVO == null) {
            memberVO = memberService.getCurrMemberInfo();
            redisTemplate.opsForValue().set(cacheKey, memberVO, Duration.ofSeconds(memberCacheConfig.getInfoCacheSeconds()));
        }

        return Result.success(memberVO);
    }


    @Operation(summary = "根据 openid 获取会员认证信息")
    @GetMapping("/openid/{openid}")
    public Result<MemberAuthDTO> getMemberByOpenid(@Parameter(name = "微信唯一身份标识") @PathVariable String openid) {
        MemberAuthDTO memberAuthInfo = memberService.getMemberByOpenid(openid);
        return Result.success(memberAuthInfo);
    }


    @Operation(summary = "获取会员地址列表")
    @GetMapping("/{memberId}/addresses")
    public Result<List<MemberAddressDTO>> listMemberAddress(
            @Parameter(name = "会员ID") @PathVariable Long memberId
    ) {
        List<MemberAddressDTO> addresses = memberService.listMemberAddress(memberId);
        return Result.success(addresses);
    }


    @Operation(summary = "找回密码 - 通过短信验证码重置密码")
    @PostMapping("/reset-password")
    public Result resetPassword(@RequestBody Map<String, String> params) {
        String mobile = params.get("mobile");
        String code = params.get("code");
        String password = params.get("password");
        String countryCode = params.getOrDefault("countryCode", "+86");

        if (StrUtil.isBlank(mobile) || StrUtil.isBlank(code) || StrUtil.isBlank(password)) {
            return Result.failed("手机号、验证码、新密码不能为空");
        }
        if (password.length() < 6 || password.length() > 18) {
            return Result.failed("密码长度需在6-18位之间");
        }

        // 验证短信验证码（使用 countryCode:mobile 组合 key）
        String codeKey = RedisConstants.LOGIN_SMS_CODE_PREFIX + countryCode + ":" + mobile;
        Object cacheValue = redisTemplate.opsForValue().get(codeKey);
        String cacheCode = cacheValue != null ? String.valueOf(cacheValue) : null;
        if (StrUtil.isBlank(cacheCode)) {
            return Result.failed("验证码已过期，请重新获取");
        }
        if (!cacheCode.equals(code)) {
            return Result.failed("验证码错误");
        }

        // 验证会员是否存在（不存在会抛 USER_NOT_EXIST）
        memberService.getMemberByMobile(mobile);

        // 更新密码
        UmsMember entity = new UmsMember();
        entity.setPassword(new Argon2PasswordEncoder(16, 200, 4, 32, 16).encode(password));
        memberService.update(entity, new LambdaUpdateWrapper<UmsMember>()
                .eq(UmsMember::getMobile, mobile));

        // 删除已使用的验证码
        redisTemplate.delete(codeKey);

        return Result.success();
    }

}
