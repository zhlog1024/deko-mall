package com.deko.mall.auth.config;

import com.deko.mall.common.result.IResultCode;
import lombok.Getter;

/**
 * AUTH认证服务错误码枚举
 * <p>
 * 码段前缀: AUTH_001 ~ AUTH_999
 * 枚举值命名: 全大写下划线 (如 AUTH_FAILED)
 */
@Getter
public enum AuthResultCode implements IResultCode {

    // 认证错误 (AUTH_001 ~ AUTH_019)
    AUTH_FAILED("AUTH_001", "认证失败"),
    TOKEN_EXPIRED("AUTH_002", "Token已过期"),
    TOKEN_INVALID("AUTH_003", "Token无效"),
    REFRESH_TOKEN_INVALID("AUTH_004", "刷新Token无效"),
    LOGIN_LOCKED("AUTH_005", "账号已被锁定"),
    CAPTCHA_ERROR("AUTH_006", "验证码错误"),
    SUPPLIER_NOT_FOUND("AUTH_007", "供应商不存在"),

    // 授权错误 (AUTH_020 ~ AUTH_029)
    ACCESS_DENIED("AUTH_020", "访问被拒绝"),
    PERMISSION_INSUFFICIENT("AUTH_021", "权限不足"),

    // 通用操作错误 (AUTH_060 ~ AUTH_069)
    OPERATION_FAILED("AUTH_060", "操作失败"),
    PARAM_INVALID("AUTH_061", "参数非法"),

    // 系统保留错误码 (AUTH_900 ~ AUTH_999)
    SYSTEM_ERROR("AUTH_900", "系统错误"),
    UNKNOWN_ERROR("AUTH_999", "未知错误");

    private final String code;
    private final String msg;

    AuthResultCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
