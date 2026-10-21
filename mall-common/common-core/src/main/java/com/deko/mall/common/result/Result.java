package com.deko.mall.common.result;

import com.deko.mall.common.i18n.I18nUtils;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结构体
 */
@Data
public class Result<T> implements Serializable {

    private String code;
    private T data;
    private String msg;

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMsg(resolve(ResultCode.SUCCESS));
        result.setData(data);
        return result;
    }

    public static <T> Result<T> failed() {
        return failed(ResultCode.SYSTEM_EXECUTION_ERROR);
    }

    /**
     * @deprecated 请使用 {@link #failed(IResultCode)} 或 {@link #failed(IResultCode, Object...)}，
     *             传入 ResultCode 枚举以确保错误消息被国际化。
     */
    @Deprecated
    public static <T> Result<T> failed(String msg) {
        return result(ResultCode.SYSTEM_EXECUTION_ERROR.getCode(), msg, null);
    }

    public static <T> Result<T> judge(boolean status) {
        return status ? success() : failed();
    }

    public static <T> Result<T> failed(IResultCode resultCode) {
        return result(resultCode.getCode(), resolve(resultCode), null);
    }

    public static <T> Result<T> failed(IResultCode resultCode, String msg) {
        return result(resultCode.getCode(), msg, null);
    }

    /**
     * 带参数的失败响应，参数会填充到国际化消息的 {0} {1} 等占位符中。
     */
    public static <T> Result<T> failed(IResultCode resultCode, Object... args) {
        String i18nMsg = I18nUtils.resolveErrorCode(resultCode.getCode(), resultCode.getMsg());
        try {
            i18nMsg = java.text.MessageFormat.format(i18nMsg, args);
        } catch (Exception ignored) {
        }
        return result(resultCode.getCode(), i18nMsg, null);
    }

    public static <T> Result<T> result(IResultCode resultCode, T data) {
        return result(resultCode.getCode(), resolve(resultCode), data);
    }

    private static <T> Result<T> result(String code, String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setData(data);
        result.setMsg(msg);
        return result;
    }

    public static boolean isSuccess(Result<?> result) {
        return result != null && ResultCode.SUCCESS.getCode().equals(result.getCode());
    }

    /** 通过 I18nUtils 解析错误码的国际化消息，找不到时回退到 ResultCode.getMsg() */
    private static String resolve(IResultCode resultCode) {
        return I18nUtils.resolveErrorCode(resultCode.getCode(), resultCode.getMsg());
    }
}
