package com.deko.mall.ums.config;

import com.deko.mall.common.result.IResultCode;
import lombok.Getter;

/**
 * UMS会员服务错误码枚举
 * <p>
 * 码段前缀: UMS_001 ~ UMS_999
 * 枚举值命名: 全大写下划线 (如 LOGIN_FAILED)
 */
@Getter
public enum UmsResultCode implements IResultCode {

    // 登录/认证错误 (UMS_001 ~ UMS_019)
    LOGIN_FAILED("UMS_001", "登录失败"),
    MEMBER_NOT_FOUND("UMS_002", "会员不存在"),
    MEMBER_LOCKED("UMS_003", "会员已被锁定"),
    SMS_CODE_ERROR("UMS_004", "短信验证码错误"),
    WECHAT_AUTH_FAILED("UMS_005", "微信授权失败"),
    LOGIN_REQUIRED("UMS_006", "请先登录"),

    // 个人资料错误 (UMS_020 ~ UMS_029)
    NICKNAME_EXISTS("UMS_020", "昵称已存在"),
    AVATAR_UPLOAD_FAILED("UMS_021", "头像上传失败"),

    // 地址错误 (UMS_030 ~ UMS_039)
    ADDRESS_NOT_FOUND("UMS_030", "地址不存在"),
    ADDRESS_LIMIT_EXCEEDED("UMS_031", "地址数量已达上限"),
    ADDRESS_TEXT_REQUIRED("UMS_032", "请输入地址文本"),

    // 收藏/关注错误 (UMS_040 ~ UMS_049)
    FAVOR_GOODS_EXISTS("UMS_040", "已收藏该商品"),
    FAVOR_SHOP_LIMIT("UMS_041", "店铺关注数量已达上限"),

    // 积分/资产错误 (UMS_050 ~ UMS_069)
    POINTS_INSUFFICIENT("UMS_050", "积分不足"),
    BALANCE_INSUFFICIENT("UMS_051", "余额不足"),
    AMOUNT_MUST_BE_POSITIVE("UMS_053", "金额必须大于0"),
    ASSET_OPERATE_TYPE_REQUIRED("UMS_054", "操作类型不能为空"),
    ASSET_INSUFFICIENT("UMS_055", "可用{0}不足"),
    ASSET_OPERATE_TYPE_UNSUPPORTED("UMS_056", "不支持的资产操作类型: {0}"),
    TRANSFER_POINTS_INSUFFICIENT("UMS_057", "转出方积分不足"),
    ONLY_POINTS_SUPPORT_TRANSFER("UMS_059", "仅积分支持转账"),

    // 通用操作错误 (UMS_060 ~ UMS_069)
    OPERATION_FAILED("UMS_060", "操作失败"),
    DATA_NOT_FOUND("UMS_061", "数据不存在"),
    PARAM_INVALID("UMS_062", "参数非法"),

    // 系统保留错误码 (UMS_900 ~ UMS_999)
    SYSTEM_ERROR("UMS_900", "系统错误"),
    UNKNOWN_ERROR("UMS_999", "未知错误");

    private final String code;
    private final String msg;

    UmsResultCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
