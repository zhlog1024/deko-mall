package com.deko.mall.auth.oauth2.oidc;

import com.deko.mall.common.result.Result;
import com.deko.mall.system.api.SystemFeignClient;
import com.deko.mall.system.dto.UserAuthInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 自定义 OIDC 用户信息服务
 */
@Service
@Slf4j
public class CustomOidcUserInfoService {

    private final SystemFeignClient systemFeignClient;

    public CustomOidcUserInfoService(SystemFeignClient systemFeignClient) {
        this.systemFeignClient = systemFeignClient;
    }

    public CustomOidcUserInfo loadUserByUsername(String username) {
        UserAuthInfo userAuthInfo = null;
        try {
            Result<UserAuthInfo> userAuthInfoResult = systemFeignClient.getUserAuthInfo(username);
            userAuthInfo = userAuthInfoResult.getData();
            if (userAuthInfo == null) {
                return null;
            }
            return new CustomOidcUserInfo(createUser(userAuthInfo));
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return null;
        }
    }

    private Map<String, Object> createUser(UserAuthInfo userAuthInfo) {
        return CustomOidcUserInfo.customBuilder()
                .username(userAuthInfo.getUsername())
                .nickname(userAuthInfo.getNickname())
                .status(userAuthInfo.getStatus())
                .phoneNumber(userAuthInfo.getMobile())
                .email(userAuthInfo.getEmail())
                .profile(userAuthInfo.getAvatar())
                .build()
                .getClaims();
    }

}
