package com.deko.mall.auth.service;

import cn.hutool.core.lang.Assert;
import com.deko.mall.auth.model.LoginUserInfo;
import com.deko.mall.auth.model.SysUserDetails;
import com.deko.mall.common.enums.StatusEnum;
import com.deko.mall.common.result.Result;
import com.deko.mall.system.api.SystemFeignClient;
import com.deko.mall.system.dto.UserAuthInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

/**
 * 系统用户信息加载实现类
 */
@Service
@RequiredArgsConstructor
public class SysUserDetailsService implements UserDetailsService {

    private final SystemFeignClient systemFeignClient;

    /**
     * 根据用户名获取用户信息(用户名、密码和角色权限)
     * <p>
     * 用户名、密码用于后续认证，认证成功之后将权限授予用户
     *
     * @param username 用户名
     * @return {@link  SysUserDetails}
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
        Result<UserAuthInfo> userAuthInfoResult = systemFeignClient.getUserAuthInfo(username);
        UserAuthInfo userAuthInfo = userAuthInfoResult.getData();

        Assert.isTrue(userAuthInfo != null, "用户不存在");

        if (!StatusEnum.ENABLE.getValue().equals(userAuthInfo.getStatus())) {
            throw new DisabledException("该账户已被禁用!");
        }

        return new SysUserDetails(userAuthInfo);
    }


    public LoginUserInfo getLoginUserInfo() {
        LoginUserInfo loginUserInfo = new LoginUserInfo();
        loginUserInfo.setId(123L);
        return loginUserInfo;
    }
}
