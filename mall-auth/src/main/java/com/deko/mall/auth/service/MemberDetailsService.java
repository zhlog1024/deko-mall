package com.deko.mall.auth.service;

import cn.hutool.core.lang.Assert;
import com.deko.mall.auth.model.MemberDetails;
import com.deko.mall.common.enums.StatusEnum;
import com.deko.mall.common.result.Result;
import com.deko.mall.common.result.ResultCode;
import com.deko.mall.common.web.exception.BizException;
import com.deko.mall.ums.api.MemberFeignClient;
import com.deko.mall.ums.dto.MemberAuthDTO;
import com.deko.mall.ums.dto.MemberRegisterDTO;
import com.deko.mall.ums.dto.WxAuthDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 商城会员用户认证服务
 */
@Service
@RequiredArgsConstructor
public class MemberDetailsService {

    private final MemberFeignClient memberFeignClient;

    /**
     * 手机号码认证方式（兼容旧接口）
     */
    public UserDetails loadUserByMobile(String mobile, String password, boolean type, Long shareUserId) {
        return loadUserByMobile(mobile, password, type, shareUserId, "+86");
    }

    /**
     * 手机号码认证方式（支持国家区号）
     *
     * @param mobile      手机号
     * @param password    密码
     * @param type        是否验证密码
     * @param shareUserId 分享人ID
     * @param countryCode 国家区号(如+86)
     * @return 用户信息
     */
    public UserDetails loadUserByMobile(String mobile, String password, boolean type, Long shareUserId, String countryCode) {
        if (countryCode == null || countryCode.isBlank()) {
            countryCode = "+86";
        }
        Result<MemberAuthDTO> result = memberFeignClient.loadUserByMobile(mobile);

        MemberAuthDTO memberAuthInfo = null;
        // 会员不存在，注册成为新会员
        if (ResultCode.USER_NOT_EXIST.getCode().equals(result.getCode())) {

            MemberRegisterDTO memberRegisterInfo = new MemberRegisterDTO();
            memberRegisterInfo.setMobile(mobile);
            memberRegisterInfo.setCountryCode(countryCode);
            memberRegisterInfo.setPassword(password);
            memberRegisterInfo.setNickName(mobile);
            // 设置分享人ID
            if (shareUserId != null) {
                memberRegisterInfo.setShareUserId(shareUserId);
            }
            // 注册会员
            Result<Long> registerMemberResult = memberFeignClient.registerMember(memberRegisterInfo);
            // 注册成功将会员信息赋值给会员认证信息
            Long memberId;
            if (Result.isSuccess(registerMemberResult) && (memberId = registerMemberResult.getData()) != null) {
                memberAuthInfo = MemberAuthDTO.builder()
                        .id(memberId)
                        .username(mobile)
                        .mobile(mobile)
                        .status(StatusEnum.ENABLE.getValue())
                        .password(password)
                        .build();
            }
        } else {
            Assert.isTrue((memberAuthInfo = result.getData()) != null, "会员认证信息失败");
            Argon2PasswordEncoder argon2PasswordEncoder = new Argon2PasswordEncoder(16, 65536, 4, 32, 16);

            // 如果密码不为空
            if (type && !argon2PasswordEncoder.matches(password, result.getData().getPassword())) {
                throw new BizException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
            }
        }
        MemberDetails userDetails = new MemberDetails(memberAuthInfo);
        if (!userDetails.isEnabled()) {
            throw new DisabledException("该账户已被禁用!");
        } else if (!userDetails.isAccountNonLocked()) {
            throw new LockedException("该账号已被锁定!");
        } else if (!userDetails.isAccountNonExpired()) {
            throw new AccountExpiredException("该账号已过期!");
        }
        return userDetails;
    }

    /**
     * 根据微信参数获取会员认证信息
     */
    public UserDetails loadUserByWxParams(Long shareUserId, String code, String encryptedData, String iv) {

        Result<MemberAuthDTO> wxUser = memberFeignClient.getWxUserInfo(WxAuthDTO.builder()
                .code(code)
                .encryptedData(encryptedData)
                .iv(iv)
                .build());
        String username = wxUser.getData().getUsername();

        // 根据 openid 获取微信用户认证信息
        Result<MemberAuthDTO> getMemberAuthInfoResult = memberFeignClient.loadUserByMobile(username);

        MemberAuthDTO memberAuthInfo = null;

        // 会员不存在，注册成为新会员
        if (ResultCode.USER_NOT_EXIST.getCode().equals(getMemberAuthInfoResult.getCode())) {
            MemberRegisterDTO memberRegisterInfo = new MemberRegisterDTO();
            memberRegisterInfo.setMobile(username);
            memberRegisterInfo.setNickName(username);
            memberRegisterInfo.setShareUserId(shareUserId);
            // 注册会员
            Result<Long> registerMemberResult = memberFeignClient.registerMember(memberRegisterInfo);
            // 注册成功将会员信息赋值给会员认证信息
            Long memberId;
            if (Result.isSuccess(registerMemberResult) && (memberId = registerMemberResult.getData()) != null) {
                memberAuthInfo = MemberAuthDTO.builder()
                        .id(memberId)
                        .username(username)
                        .mobile(username)
                        .status(StatusEnum.ENABLE.getValue())
                        .build();
            }
        } else {
            Assert.isTrue((memberAuthInfo = getMemberAuthInfoResult.getData()) != null, "会员认证信息失败");
        }

        // 用户不存在
        if (memberAuthInfo == null) {
            throw new UsernameNotFoundException(ResultCode.USER_NOT_EXIST.getMsg());
        }

        UserDetails userDetails = new MemberDetails(memberAuthInfo);
        if (!userDetails.isEnabled()) {
            throw new DisabledException("该账户已被禁用!");
        } else if (!userDetails.isAccountNonLocked()) {
            throw new LockedException("该账号已被锁定!");
        } else if (!userDetails.isAccountNonExpired()) {
            throw new AccountExpiredException("该账号已过期!");
        }
        return userDetails;
    }

}
