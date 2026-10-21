package com.deko.mall.sms.config;

import com.deko.mall.common.result.IResultCode;
import lombok.Getter;

/**
 * SMS营销服务错误码枚举
 * <p>
 * 码段前缀: SMS_001 ~ SMS_999
 * 枚举值命名: 全大写下划线 (如 COUPON_NOT_FOUND)
 */
@Getter
public enum SmsResultCode implements IResultCode {

    // 优惠券错误 (SMS_001 ~ SMS_019)
    COUPON_NOT_FOUND("SMS_001", "优惠券不存在"),
    COUPON_EXPIRED("SMS_002", "优惠券已过期"),
    COUPON_RECEIVED("SMS_003", "已领取该优惠券"),
    COUPON_RECEIVE_LIMIT("SMS_004", "优惠券领取已达上限"),

    // 促销活动错误 (SMS_020 ~ SMS_029)
    PROMOTION_NOT_FOUND("SMS_020", "促销活动不存在"),
    PROMOTION_ENDED("SMS_021", "促销活动已结束"),
    PROMOTION_NOT_STARTED("SMS_022", "促销活动未开始"),

    // 秒杀错误 (SMS_030 ~ SMS_039)
    SECKILL_NOT_FOUND("SMS_030", "秒杀活动不存在"),
    SECKILL_ENDED("SMS_031", "秒杀活动已结束"),
    SECKILL_STOCK_EMPTY("SMS_032", "秒杀库存已空"),

    // 模板错误 (SMS_040 ~ SMS_049)
    TEMPLATE_NOT_FOUND("SMS_040", "模板不存在"),

    // 直播错误 (SMS_050 ~ SMS_057)
    LIVE_ROOM_NOT_FOUND("SMS_050", "直播间不存在"),
    LIVE_ROOM_STATUS_INVALID("SMS_051", "直播间状态非法"),
    LIVE_GOODS_EXISTS("SMS_052", "商品已在直播间"),
    LIVE_GOODS_NOT_FOUND("SMS_053", "直播商品不存在"),
    LIVE_STREAM_NAME_EXISTS("SMS_054", "直播流名已存在"),
    LIVE_NOT_LIVING("SMS_055", "直播间未开播"),
    LIVE_CHAT_NOT_OPEN("SMS_056", "直播间未开启聊天"),
    LIVE_MEMBER_NOT_LOGIN("SMS_057", "请先登录"),

    // 通用操作错误 (SMS_060 ~ SMS_069)
    OPERATION_FAILED("SMS_060", "操作失败"),
    DATA_NOT_FOUND("SMS_061", "数据不存在"),
    PARAM_INVALID("SMS_062", "参数非法"),

    // 系统保留错误码 (SMS_900 ~ SMS_999)
    SYSTEM_ERROR("SMS_900", "系统错误"),
    UNKNOWN_ERROR("SMS_999", "未知错误");

    private final String code;
    private final String msg;

    SmsResultCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
