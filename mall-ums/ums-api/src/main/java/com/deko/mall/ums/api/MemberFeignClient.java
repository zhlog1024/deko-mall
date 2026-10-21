package com.deko.mall.ums.api;

import com.deko.mall.common.result.Result;
import com.deko.mall.ums.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会员Feign客户端
 */
@FeignClient(name = "mall-ums", contextId = "member")
public interface MemberFeignClient {

    /**
     * 新增会员
     */
    @PostMapping("/feign-api/v1/members")
    Result<Long> registerMember(@RequestBody MemberRegisterDTO member);

    /**
     * 获取会员的 openid
     */
    @GetMapping("/feign-api/v1/members/get/openid/{id}")
    Result<String> getMemberOpenId(@PathVariable Long id);

    @PostMapping("/feign-api/v1/members/mobile/wx/params")
    Result<MemberAuthDTO> getWxUserInfo(@RequestBody WxAuthDTO wxAuthDTO);

    @GetMapping("/feign-api/v1/members/openid/wx/{code}")
    Result<String> getOpenidByCode(@PathVariable String code);

    /**
     * 根据手机号获取会员认证信息
     *
     * @param mobile 手机号
     * @return 会员认证信息
     */
    @GetMapping("/feign-api/v1/members/mobile/{mobile}")
    Result<MemberAuthDTO> loadUserByMobile(@PathVariable String mobile);


    /**
     * 获取会员地址列表
     */
    @GetMapping("/feign-api/v1/members/{memberId}/addresses")
    Result<List<MemberAddressDTO>> listMemberAddresses(@PathVariable Long memberId);

    /**
     * 获取用户信息
     */
    @PostMapping("/feign-api/v1/members/info/{userId}")
    Result<MemberInfoDTO> info(@PathVariable Long userId);
}


