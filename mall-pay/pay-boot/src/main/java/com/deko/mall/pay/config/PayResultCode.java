package com.deko.mall.pay.config;

import com.deko.mall.common.result.IResultCode;
import lombok.Getter;

/**
 * PAY支付服务错误码枚举
 */
@Getter
public enum PayResultCode implements IResultCode {

    // 通用配置错误 (PAY_001 ~ PAY_019)
    PAY_CONFIG_INCOMPLETE("PAY_001", "支付配置不完整"),
    PAY_CONFIG_NOT_FOUND("PAY_002", "未找到支付配置"),
    PAY_CONFIG_MCH_MISSING("PAY_003", "缺少商户号"),
    PAY_CONFIG_KEY_MISSING("PAY_004", "缺少API密钥"),
    PAY_CONFIG_CERT_MISSING("PAY_005", "缺少证书"),
    PAY_CONFIG_EXT_EMPTY("PAY_006", "扩展配置为空"),
    PAY_CONFIG_ACTIVE_EXISTS("PAY_007", "存在启用的支付配置，请先停用"),

    // 支付错误 (PAY_020 ~ PAY_039)
    PAY_ORDER_NOT_FOUND("PAY_020", "支付单不存在"),
    PAY_ORDER_IN_PROGRESS("PAY_021", "支付单正在支付中"),
    PAY_CREATE_FAILED("PAY_022", "创建支付单失败"),
    PAY_NO_AVAILABLE_METHOD("PAY_023", "暂无可用的支付方式"),
    PAY_AMOUNT_MISMATCH("PAY_024", "支付金额不匹配"),
    PAY_LOCK_FAILED("PAY_025", "获取支付锁失败"),
    PAY_CHANNEL_NOT_SUPPORTED("PAY_026", "不支持的支付渠道"),
    PAY_CALLBACK_EXCEPTION("PAY_027", "支付回调异常"),
    PAY_REFUND_CALLBACK_EXCEPTION("PAY_028", "退款回调异常"),
    PAY_QUERY_STATUS_ERROR("PAY_029", "查询支付状态异常"),
    PAY_DEVICE_TYPE_NOT_SUPPORTED("PAY_030", "不支持的设备类型"),
    PAY_FULL_DEDUCTION_FAILED("PAY_031", "全额抵扣失败"),
    PAY_BODY_READ_ERROR("PAY_032", "读取请求体异常"),
    PAY_KEY_PARSE_FAILED("PAY_033", "解析公钥失败"),
    PAY_SIGN_FAILED("PAY_034", "支付签名失败"),
    PAY_VERIFY_FAILED("PAY_035", "支付验证失败"),
    PAY_HTTP_REQUEST_ERROR("PAY_036", "HTTP请求失败"),
    PAY_ORDER_STATUS_ERROR("PAY_037", "支付单状态异常"),
    PAY_ORDER_STATUS_CANNOT_PAY("PAY_038", "支付单状态不可支付"),
    PAY_CALLBACK_PROCESS_ERROR("PAY_039", "处理支付回调异常"),

    // 微信支付错误 (PAY_040 ~ PAY_049)
    WX_CONFIG_NOT_FOUND("PAY_040", "未找到微信支付配置"),
    WX_CONFIG_INCOMPLETE("PAY_041", "微信支付配置不完整"),
    WX_CERT_SERIAL_MISSING("PAY_042", "缺少商户证书序列号"),
    WX_PRIVATE_KEY_MISSING("PAY_043", "缺少商户私钥"),
    WX_API_KEY_MISSING("PAY_044", "缺少API V3密钥"),
    WX_CERT_FORMAT_ERROR("PAY_045", "证书格式不正确"),
    WX_CREATE_ORDER_FAILED("PAY_046", "创建微信支付单失败"),
    WX_PREPAY_ID_EMPTY("PAY_047", "prepay_id为空"),
    WX_CALLBACK_FAILED("PAY_048", "微信支付回调失败"),

    // 支付渠道请求/策略错误 (PAY_050 ~ PAY_059)
    CHANNEL_REQUEST_NOT_FOUND("PAY_050", "渠道请求记录不存在"),
    CHANNEL_REQUEST_STATUS_INVALID("PAY_051", "渠道请求状态不正确"),
    PAY_POST_PROCESS_ERROR("PAY_052", "处理支付后续逻辑异常"),
    CHANNEL_REQUEST_CREATE_FAILED("PAY_053", "创建支付渠道请求失败"),
    CHANNEL_REQUEST_UPDATE_FAILED("PAY_054", "更新支付渠道请求失败"),
    CHANNEL_NOT_CONFIGURED("PAY_055", "支付渠道未配置"),
    CHANNEL_CONFIG_NOT_FOUND("PAY_056", "支付渠道配置不存在"),
    CHANNEL_INIT_FAILED("PAY_057", "支付渠道初始化失败"),
    PAY_REQUEST_NOT_FOUND("PAY_058", "支付请求记录不存在"),
    PAY_REQUEST_STATUS_INVALID("PAY_059", "支付请求状态不正确"),

    // 支付宝错误 (PAY_060 ~ PAY_079)
    ALIPAY_CONFIG_INCOMPLETE("PAY_060", "支付宝配置不完整"),
    ALIPAY_INIT_FAILED("PAY_061", "支付宝配置初始化失败"),
    ALIPAY_CREATE_ORDER_FAILED("PAY_062", "创建支付宝订单失败"),
    ALIPAY_CALLBACK_FAILED("PAY_063", "支付宝回调异常"),
    ALIPAY_APP_PAY_FAILED("PAY_064", "创建支付宝APP支付失败"),
    ALIPAY_QUERY_FAILED("PAY_065", "查询支付宝订单状态失败"),

    // 退款错误 (PAY_120 ~ PAY_139)
    REFUND_APPLY_FAILED("PAY_120", "退款申请失败"),
    REFUND_AMOUNT_EXCEED("PAY_121", "退款金额超过可退金额"),
    REFUND_NOT_FOUND("PAY_122", "未找到有效的业务支付单"),
    REFUND_CHANNEL_NOT_FOUND("PAY_123", "未找到有效的渠道支付请求"),
    REFUND_CALLBACK_PROCESS_ERROR("PAY_124", "处理退款回调异常"),

    // 线下支付错误 (PAY_190 ~ PAY_199)
    OFFLINE_PAY_RECORD_NOT_FOUND("PAY_190", "线下支付记录不存在"),
    OFFLINE_PAY_ALREADY_AUDITED("PAY_191", "该线下支付记录不存在或已审核"),
    OFFLINE_PAY_CHANNEL_NOT_FOUND("PAY_192", "渠道支付记录不存在"),

    // 通用操作错误 (PAY_900 ~ PAY_999)
    SYSTEM_ERROR("PAY_900", "系统错误"),
    OPERATION_FAILED("PAY_901", "操作失败"),
    UNKNOWN_ERROR("PAY_999", "未知错误");

    private final String code;
    private final String msg;

    PayResultCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
