package com.deko.mall.system.api;

import com.deko.mall.common.result.Result;
import com.deko.mall.common.web.config.FeignDecoderConfig;
import com.deko.mall.system.dto.UserAuthInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "mall-system", configuration = {FeignDecoderConfig.class})
public interface SystemFeignClient {


    @GetMapping("/feign-api/v1/system/{username}/authInfo")
    Result<UserAuthInfo> getUserAuthInfo(@PathVariable String username);

    /**
     * 为供应商分配权限
     *
     * @param userId 供应商账号
     * @return 分配结果
     */
    @PostMapping("/feign-api/v1/system/permissions/assign")
    Result<Long> assignPermission(
            @RequestParam("userId") Long userId
    );


    /**
     * 为供应商分配权限
     *
     * @param loginName    供应商账号
     * @param supplierName 供应商名称
     * @return 分配结果
     */
    @PostMapping("/feign-api/v1/system/permissions/user/create")
    Result<Long> createPermissionUser(
            @RequestParam("loginName") String loginName,
            @RequestParam("supplierName") String supplierName,
            @RequestParam("password") String password
    );
}
