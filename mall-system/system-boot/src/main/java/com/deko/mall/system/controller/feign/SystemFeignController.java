package com.deko.mall.system.controller.feign;

import com.deko.mall.common.enums.SystemTypeEnum;
import com.deko.mall.common.result.Result;
import com.deko.mall.system.api.SystemFeignClient;
import com.deko.mall.system.converter.UserConverter;
import com.deko.mall.system.dto.UserAuthInfo;
import com.deko.mall.system.model.entity.SysUser;
import com.deko.mall.system.model.form.UserForm;
import com.deko.mall.system.service.SysRoleService;
import com.deko.mall.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * 用户控制器
 */
@Tag(name = "01.用户接口")
@RestController
@RequestMapping("/feign-api/v1/system")
@RequiredArgsConstructor
@Slf4j
public class SystemFeignController implements SystemFeignClient {

    private final SysUserService userService;

    private final SysUserService sysUserService;
    private final UserConverter userConverter;
    private final SysRoleService sysRoleService;
    @Value("${permission.supplier.submit.role-codes:SUPPLIER_BASIC}")
    private String submitRoleCode; // 提交后角色编码，默认为SUPPLIER_BASIC
    @Value("${permission.supplier.approved.role-codes:SUPPLIER_FULL}")
    private String approvedRoleCode; // 审核后角色编码，默认为SUPPLIER_FULL


    @Operation(summary = "获取用户认证信息", hidden = true)
    @GetMapping("/{username}/authInfo")
    public Result<UserAuthInfo> getUserAuthInfo(
            @Parameter(description = "用户名") @PathVariable String username
    ) {
        UserAuthInfo userAuthInfo = userService.getUserAuthInfo(username);
        return Result.success(userAuthInfo);
    }


    /**
     * 为供应商分配权限
     *
     * @param userId 供应商ID
     * @return 分配结果
     */
    @Override
    @PostMapping("/permissions/assign")
    public Result<Long> assignPermission(
            @RequestParam("userId") Long userId
    ) {
        log.info("【Feign调用】为供应商分配权限: userId={}", userId);

        try {
            SysUser user = sysUserService.getById(userId);
            UserForm userForm = userConverter.entity2Form(user);

            // 通过角色编码获取角色ID列表
            List<String> roleCodes = Collections.singletonList(approvedRoleCode);
            List<Long> roleIds = sysRoleService.getRoleIdsByCodes(roleCodes);
            log.info("【Feign调用】根据角色编码获取到的角色IDs: {}, 角色编码: {}", roleIds, approvedRoleCode);

            userForm.setRoleIds(roleIds);
            sysUserService.updateUser(userId, userForm);
            return Result.success(userId);
        } catch (Exception e) {
            log.error("【Feign调用】为供应商分配权限异常", e);
            return Result.failed("分配供应商权限失败：" + e.getMessage());
        }
    }

    @Override
    @PostMapping("/permissions/user/create")
    public Result<Long> createPermissionUser(
            @RequestParam("loginName") String loginName,
            @RequestParam("supplierName") String supplierName,
            @RequestParam("password") String password
    ) {

        log.info("【Feign调用】为供应商创建用户: loginName={}, supplierName={}", loginName, supplierName);
        try {
            UserForm userForm = new UserForm();
            userForm.setUsername(loginName);
            userForm.setNickname(supplierName);
            userForm.setPassword(password);

            // 通过角色编码获取角色ID列表
            List<String> roleCodes = Collections.singletonList(submitRoleCode);
            List<Long> roleIds = sysRoleService.getRoleIdsByCodes(roleCodes);
            log.info("【Feign调用】根据角色编码获取到的角色IDs: {}, 角色编码: {}", roleIds, submitRoleCode);

            userForm.setRoleIds(roleIds);
            userForm.setDeptId(0L);
            userForm.setSystemType(SystemTypeEnum.SUPPLIER.getValue());
            Long userId = sysUserService.saveUser(userForm);
            return Result.success(userId);
        } catch (Exception e) {
            log.error("【Feign调用】为供应商创建系统用户", e);
            return Result.failed("供应商用户创建失败：" + e.getMessage());
        }
    }

}
