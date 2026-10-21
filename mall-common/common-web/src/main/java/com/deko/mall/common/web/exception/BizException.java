package com.deko.mall.common.web.exception;

import com.deko.mall.common.i18n.I18nUtils;
import com.deko.mall.common.result.IResultCode;
import com.deko.mall.common.result.ResultCode;
import lombok.Getter;

/**
 * 自定义业务异常
 */
@Getter
public class BizException extends RuntimeException {

    private final IResultCode resultCode;

    // 推荐构造器 (携带 IResultCode)

    public BizException(IResultCode resultCode) {
        super(I18nUtils.resolveErrorCode(resultCode.getCode(), resultCode.getMsg()));
        this.resultCode = resultCode;
    }

    public BizException(IResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }

    public BizException(IResultCode resultCode, String message, Throwable cause) {
        super(message, cause);
        this.resultCode = resultCode;
    }

    public BizException(IResultCode resultCode, Throwable cause) {
        super(I18nUtils.resolveErrorCode(resultCode.getCode(), resultCode.getMsg()), cause);
        this.resultCode = resultCode;
    }

    /**
     * 带参数的构造器，参数会填充到国际化消息的 {0} {1} 等占位符中。
     */
    public BizException(IResultCode resultCode, Object... args) {
        super(formatI18nMsg(resultCode, args));
        this.resultCode = resultCode;
    }

    private static String formatI18nMsg(IResultCode resultCode, Object... args) {
        String msg = I18nUtils.resolveErrorCode(resultCode.getCode(), resultCode.getMsg());
        try {
            return java.text.MessageFormat.format(msg, args);
        } catch (Exception ignored) {
            return msg;
        }
    }

    // 兼容构造器 (保留向后兼容)

    /**
     * @deprecated 请使用 {@link #BizException(IResultCode)} 并传入对应的 ResultCode 枚举。
     */
    @Deprecated
    public BizException(String message) {
        super(message);
        this.resultCode = ResultCode.SYSTEM_EXECUTION_ERROR;
    }

    /**
     * @deprecated 请使用 {@link #BizException(IResultCode, String, Throwable)}。
     */
    @Deprecated
    public BizException(String message, Throwable cause) {
        super(message, cause);
        this.resultCode = ResultCode.SYSTEM_EXECUTION_ERROR;
    }

    @Deprecated
    public BizException(Throwable cause) {
        super(cause);
        this.resultCode = ResultCode.SYSTEM_EXECUTION_ERROR;
    }
}
