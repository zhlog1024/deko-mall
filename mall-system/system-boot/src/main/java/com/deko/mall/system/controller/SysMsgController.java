package com.deko.mall.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import com.deko.mall.system.model.entity.SysMsgRecord;
import com.deko.mall.system.model.query.SysMsgRecordQuery;
import com.deko.mall.system.service.msg.SysMsgService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * <p>
 * 获取验证码、退出登录等接口
 * 注：登录接口不在此控制器，在过滤器OAuth2TokenEndpointFilter拦截端点(/oauth2/token)处理
 */
@Tag(name = "消息接口")
@RestController
@RequestMapping("/api/v1/msg")
@RequiredArgsConstructor
public class SysMsgController {

    private final SysMsgService sysMsgService;


    @Operation(summary = "发送登录短信验证码")
    @PostMapping("/login/sms_code")
    public Result sendLoginSmsCode(
            @Parameter(description = "手机号") @RequestParam String mobile,
            @Parameter(description = "国家区号(如+86)") @RequestParam(required = false, defaultValue = "+86") String countryCode
    ) {
        boolean result = sysMsgService.sendLoginSmsCode(mobile, countryCode);
        return Result.judge(result);
    }


    @Operation(summary = "发送注册短信验证码")
    @PostMapping("/register/sms_code")
    public Result sendRegistrationSmsCode(
            @Parameter(description = "手机号") @RequestParam String mobile,
            @Parameter(description = "国家区号(如+86)") @RequestParam(required = false, defaultValue = "+86") String countryCode
    ) {
        boolean result = sysMsgService.sendRegisterSmsCode(mobile, countryCode);
        return Result.judge(result);
    }


    @Operation(summary = "短信记录分页列表")
    @GetMapping("/records/page")
    @PreAuthorize("@pms.hasPermission('sys:msg:record:query')")
    public PageResult<SysMsgRecord> listRecordsPage(
            @ParameterObject SysMsgRecordQuery queryVO
    ) {
        IPage<SysMsgRecord> result = sysMsgService.listRecordsPage(queryVO);
        return PageResult.success(result);
    }


}
