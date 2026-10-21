package com.deko.mall.oms.config;

import com.deko.mall.common.result.IResultCode;
import lombok.Getter;

/**
 * OMS订单服务错误码枚举
 * <p>
 * 码段前缀: OMS_001 ~ OMS_999
 * 枚举值命名: 全大写下划线 (如 ORDER_NOT_FOUND)
 * 消息使用 MessageFormat 占位符格式，如: "订单状态为{0}，无法{1}"
 */
@Getter
public enum OmsResultCode implements IResultCode {

    // 订单基础错误 (OMS_001 ~ OMS_019)
    ORDER_NOT_FOUND("OMS_001", "订单不存在"),
    ORDER_STATUS_INVALID("OMS_002", "订单状态无效"),
    ORDER_ALREADY_SHIPPED("OMS_003", "订单已发货"),
    ORDER_ALREADY_PAID("OMS_004", "订单已支付"),
    ORDER_CANNOT_CANCEL("OMS_005", "订单无法取消"),
    ORDER_STATUS_CANNOT_PAY("OMS_006", "订单状态不可支付"),
    ORDER_STATUS_CANNOT_SHIP("OMS_007", "订单状态不可发货"),
    ORDER_STATUS_CANNOT_REFUND("OMS_008", "订单状态不可退款"),
    ORDER_LOGISTICS_NOT_EXIST("OMS_009", "订单物流信息不存在"),
    ORDER_RECEIVE_ADDRESS_NOT_EXIST("OMS_010", "收货地址不存在"),
    ORDER_ASSOCIATED_NOT_EXIST("OMS_011", "关联订单不存在"),
    ORDER_SUBMIT_FAILED("OMS_017", "订单提交失败:{0}"),
    ORDER_GOODS_NOT_EXIST("OMS_018", "商品不存在"),
    ORDER_GOODS_ID_EMPTY_OR_QUANTITY_INVALID("OMS_019", "商品ID不能为空且数量必须大于0"),

    // 售后错误 (OMS_012 ~ OMS_016)
    AFTER_SALE_NOT_EXIST("OMS_012", "售后申请不存在"),
    AFTER_SALE_GOODS_CANNOT_EMPTY("OMS_013", "售后商品不能为空"),
    AFTER_SALE_STATUS_CANNOT_CANCEL("OMS_014", "当前售后状态不支持取消"),
    AFTER_SALE_STATUS_CANNOT_AUDIT("OMS_015", "当前售后状态不支持审核"),
    AFTER_SALE_ORDER_STATUS_NOT_SATISFIED("OMS_016", "当前订单状态不满足售后条件"),

    // 积分抵扣错误
    INTEGRAL_DEDUCTION_FAILED("OMS_021", "积分扣减失败"),

    // 支付错误 (OMS_023 ~ OMS_040)
    PAY_CONFIG_NOT_EXIST("OMS_023", "支付配置不存在"),
    PAY_NO_AVAILABLE_METHOD("OMS_024", "暂无可用的支付方式"),
    PAY_ORDER_IN_PROGRESS("OMS_025", "支付单正在支付中"),
    PAY_ORDER_NOT_EXIST("OMS_026", "支付单不存在"),
    PAY_ORDER_STATUS_ABNORMAL("OMS_027", "支付单状态异常"),
    PAY_CHANNEL_NOT_SUPPORTED("OMS_028", "不支持的支付渠道"),
    PAY_WX_CONFIG_INCOMPLETE("OMS_029", "微信支付配置不完整"),
    PAY_ZFB_CONFIG_INCOMPLETE("OMS_030", "支付宝配置不完整"),
    PAY_CHANNEL_REQUEST_RECORD_NOT_EXIST("OMS_031", "渠道请求记录不存在"),
    PAY_CHANNEL_REQUEST_NOT_EXIST("OMS_032", "渠道请求不存在"),
    PAY_CREATE_CHANNEL_REQUEST_FAILED("OMS_033", "创建支付渠道请求失败"),
    PAY_UPDATE_CHANNEL_REQUEST_FAILED("OMS_034", "更新支付渠道请求失败"),
    PAY_CALLBACK_HANDLE_EXCEPTION("OMS_035", "处理支付回调异常"),
    PAY_REFUND_CALLBACK_HANDLE_EXCEPTION("OMS_036", "处理退款回调异常"),
    PAY_QUERY_STATUS_EXCEPTION("OMS_037", "查询支付状态异常"),
    PAY_CREATE_WX_ORDER_FAILED("OMS_038", "创建微信支付单失败"),
    PAY_CREATE_ZFB_ORDER_FAILED("OMS_039", "创建支付宝订单失败"),

    // 物流错误 (OMS_041 ~ OMS_045)
    LOGISTICS_WAYBILL_NOT_EXIST("OMS_041", "物流运单信息不存在"),
    LOGISTICS_STRATEGY_NOT_FOUND("OMS_042", "未找到对应的物流策略"),
    LOGISTICS_COMPENSATE_QUERY_FAILED("OMS_043", "补偿查询物流轨迹失败"),
    LOGISTICS_KD100_CALLBACK_FAILED("OMS_044", "处理快递100回调失败"),
    LOGISTICS_DEVICE_TYPE_NOT_SUPPORTED("OMS_045", "不支持的设备类型"),

    ORDER_REGION_NOT_SUPPORT("OMS_050", "您所在区域不支持以下商品销售:{0}"),

    // 动态消息模板 (用于占位符替换)
    ORDER_STATUS_CANNOT_OPERATE("OMS_051", "订单状态为{0}，无法{1}"),
    ORDER_AMOUNT_MISMATCH("OMS_052", "订单金额不匹配，期望{0}，实际{1}"),
    AFTER_SALE_QUANTITY_EXCEED("OMS_053", "售后商品数量不能超过购买数量，当前可售后{0}件"),
    PAY_AMOUNT_MISMATCH("OMS_054", "支付金额不匹配，期望{0}，实际{1}"),
    LOGISTICS_TRACK_QUERY_FAILED("OMS_055", "查询物流轨迹失败:{0}"),

    // 通用操作错误 (OMS_060 ~ OMS_069)
    OPERATION_EXECUTE_FAILED("OMS_060", "操作执行失败"),
    DATA_NOT_FOUND("OMS_061", "数据不存在"),
    PARAM_INVALID("OMS_062", "参数非法"),
    STATUS_UPDATE_FAILED("OMS_063", "状态更新失败"),
    SAVE_FAILED("OMS_064", "保存失败"),
    DELETE_FAILED("OMS_065", "删除失败"),
    EXPORT_FAILED("OMS_066", "导出失败"),
    IMPORT_FAILED("OMS_067", "导入失败"),
    REQUEST_TIMEOUT("OMS_068", "请求超时"),
    SERVICE_UNAVAILABLE("OMS_069", "服务暂不可用"),

    // 购物车错误 (OMS_070 ~ OMS_079)
    CART_EMPTY("OMS_070", "购物车为空"),
    CART_GOODS_NOT_EXIST("OMS_071", "购物车商品不存在"),
    CART_ADD_SUCCESS("OMS_072", "已添加到购物车"),
    CART_UPDATE_SUCCESS("OMS_073", "购物车更新成功"),
    CART_REMOVE_SUCCESS("OMS_074", "已从购物车移除"),
    CART_CLEAR_SUCCESS("OMS_075", "购物车已清空"),

    // 订单操作确认错误 (OMS_080 ~ OMS_089)
    ORDER_CONFIRM_FAILED("OMS_080", "订单确认失败"),
    ORDER_COMPLETE_FAILED("OMS_081", "订单完成失败"),
    ORDER_SHIP_FAILED("OMS_082", "订单发货失败"),
    ORDER_CANCEL_FAILED("OMS_083", "订单取消失败"),
    ORDER_PAY_FAILED("OMS_084", "订单支付失败"),

    // 订单查询错误 (OMS_090 ~ OMS_099)
    ORDER_LIST_QUERY_FAILED("OMS_090", "获取订单列表失败"),
    ORDER_DETAIL_QUERY_FAILED("OMS_091", "获取订单详情失败"),
    ORDER_ITEMS_QUERY_FAILED("OMS_092", "获取订单项列表失败"),
    AFTER_SALE_LIST_QUERY_FAILED("OMS_093", "获取售后列表失败"),
    AFTER_SALE_DETAIL_QUERY_FAILED("OMS_094", "获取售后详情失败"),
    LOGISTICS_LIST_QUERY_FAILED("OMS_095", "获取物流列表失败"),
    LOGISTICS_DETAIL_QUERY_FAILED("OMS_096", "获取物流详情失败"),

    // 余额/账户错误 (OMS_100 ~ OMS_109)
    ACCOUNT_BALANCE_INSUFFICIENT("OMS_100", "账户余额不足"),
    ACCOUNT_FROZEN("OMS_101", "账户已被冻结"),
    ACCOUNT_CLOSED("OMS_102", "账户已注销"),
    INTEGRAL_INSUFFICIENT("OMS_103", "积分不足"),
    INTEGRAL_QUERY_FAILED("OMS_104", "查询积分失败"),

    // 订单互斥操作错误 (OMS_110 ~ OMS_119)
    ORDER_PAYING_DUPLICATE("OMS_110", "订单正在支付中，请勿重复支付"),
    ORDER_LOCKED("OMS_111", "订单已被锁定"),
    ORDER_CONFLICT("OMS_112", "订单操作冲突"),
    AFTER_SALE_APPLIED("OMS_113", "售后申请已提交，请勿重复操作"),
    AFTER_SALE_PROCESSING("OMS_114", "售后正在处理中"),

    // 外部服务调用错误 (OMS_130 ~ OMS_139)
    WECHAT_API_CALL_FAILED("OMS_130", "微信API调用失败"),
    ALIPAY_API_CALL_FAILED("OMS_131", "支付宝API调用失败"),
    THIRD_PARTY_API_CALL_FAILED("OMS_132", "第三方API调用失败"),

    // 库存错误 (OMS_150 ~ OMS_159)
    STOCK_INSUFFICIENT("OMS_150", "库存不足"),
    STOCK_LOCK_FAILED("OMS_151", "库存锁定失败"),
    STOCK_UNLOCK_FAILED("OMS_152", "库存解锁失败"),
    STOCK_DEDUCT_FAILED("OMS_153", "库存扣减失败"),
    STOCK_RETURN_FAILED("OMS_154", "库存返还失败"),

    // 优惠券/活动错误 (OMS_160 ~ OMS_169)
    COUPON_NOT_EXIST("OMS_160", "优惠券不存在"),
    COUPON_USED("OMS_161", "优惠券已使用"),
    COUPON_EXPIRED("OMS_162", "优惠券已过期"),
    PROMOTION_NOT_EXIST("OMS_164", "促销活动不存在"),
    PROMOTION_EXPIRED("OMS_165", "促销活动已结束"),

    // 支付扩展错误 (OMS_300 ~ OMS_309)
    PAY_POST_PROCESS_EXCEPTION("OMS_300", "处理支付成功后续逻辑异常"),
    PAY_FULL_DEDUCTION_FAILED("OMS_302", "全额抵扣失败"),
    PAY_LOCK_ACQUIRE_FAILED("OMS_296", "获取支付锁失败，请重试"),
    REFUND_APPLY_EXCEPTION("OMS_299", "退款申请处理异常"),

    // 订单导出错误 (OMS_120 ~ OMS_129)
    ORDER_EXPORT_FAILED("OMS_120", "订单导出失败:{0}"),
    AFTER_SALE_EXPORT_FAILED("OMS_121", "售后单导出失败:{0}"),

    // 购物车/订单新增错误 (OMS_293 ~ OMS_331)
    ORDER_ID_CANNOT_BE_EMPTY("OMS_293", "订单ID不能为空"),
    CALLBACK_PARAMS_CANNOT_BE_EMPTY("OMS_306", "回调参数不能为空"),
    ORDER_NOT_FOUND_CANNOT_SETTLE("OMS_309", "订单不存在，无法处理结算结果"),
    LOGISTICS_TRACK_SUBSCRIBE_FAILED("OMS_311", "物流轨迹订阅失败:{0}"),
    LOGISTICS_CALLBACK_PROCESS_FAILED("OMS_312", "处理物流回调失败:{0}"),
    LOGISTICS_TRACK_PARSE_FAILED("OMS_313", "解析物流轨迹失败:{0}"),
    ORDER_STATUS_CANNOT_CANCEL("OMS_314", "订单状态不可取消"),
    QUANTITY_MUST_BE_POSITIVE("OMS_315", "购买数量必须大于0"),
    CART_NO_SELECTED_GOODS("OMS_317", "购物车中没有选中的商品"),
    PAY_CREATE_FAILED("OMS_318", "创建支付单失败"),
    ORDER_STATUS_CANNOT_CONFIRM_RECEIPT("OMS_319", "订单状态不正确，无法确认收货"),
    ORDER_STATUS_NOT_PENDING_SHIP("OMS_320", "订单状态不是待发货状态，无法进行线下发货"),
    NO_EXPORTABLE_ORDER_DATA("OMS_321", "未查询到可导出的订单数据"),
    AFTER_SALE_ALREADY_IN_PROGRESS("OMS_322", "该商品已有进行中的售后申请"),
    REFUND_AMOUNT_CANNOT_BE_NEGATIVE("OMS_323", "退款金额不能为负数"),
    REFUND_AMOUNT_EXCEEDS_PAID("OMS_324", "退款金额超过订单实付金额"),
    AFTER_SALE_ORDER_NOT_EXIST("OMS_325", "售后单不存在"),
    AFTER_SALE_STATUS_CANNOT_FILL_LOGISTICS("OMS_326", "当前售后状态不支持填写物流信息"),
    PAY_ORDER_NO_VALID_ORDER("OMS_327", "该支付单下无有效订单"),
    CART_GOODS_NOT_SUPPORT("OMS_328", "该商品不支持加入购物车"),
    SKU_NOT_FOUND("OMS_330", "商品SKU不存在:{0}"),
    GOODS_NOT_EXIST_OR_OFF_SHELF("OMS_331", "商品不存在或已下架:{0}"),

    // 系统保留错误码 (OMS_900 ~ OMS_999)
    SYSTEM_ERROR("OMS_900", "系统错误"),
    SYSTEM_BUSY("OMS_901", "系统繁忙，请稍后再试"),
    SYSTEM_MAINTAIN("OMS_902", "系统维护中"),
    DATA_ERROR("OMS_903", "数据错误"),
    UNKNOWN_ERROR("OMS_999", "未知错误");

    private final String code;
    private final String msg;

    OmsResultCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
