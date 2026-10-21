package com.deko.mall.pms.config;

import com.deko.mall.common.result.IResultCode;
import lombok.Getter;

/**
 * PMS商品服务错误码枚举
 * <p>
 * 码段前缀: PMS_001 ~ PMS_999
 * 枚举值命名: 全大写下划线 (如 GOODS_NOT_FOUND)
 */
@Getter
public enum PmsResultCode implements IResultCode {

    // 商品错误 (PMS_001 ~ PMS_009)
    GOODS_NOT_FOUND("PMS_001", "商品不存在"),
    GOODS_OFF_SHELF("PMS_002", "商品已下架"),
    GOODS_OUT_OF_STOCK("PMS_003", "商品库存不足"),
    GOODS_SKU_NOT_EXIST("PMS_004", "商品SKU不存在"),
    GOODS_CATEGORY_NOT_EXIST("PMS_005", "商品分类不存在"),
    GOODS_SPEC_NOT_EXIST("PMS_006", "商品规格不存在"),
    GOODS_PRICE_INVALID("PMS_007", "商品价格无效"),
    GOODS_IMAGE_UPLOAD_FAILED("PMS_008", "商品图片上传失败"),

    // 评论错误 (PMS_009 ~ PMS_014)
    COMMENT_NOT_FOUND("PMS_009", "评论不存在"),
    COMMENT_ALREADY_EXISTS("PMS_010", "该商品已评论，请勿重复提交"),
    COMMENT_ITEMS_EMPTY("PMS_011", "评论项不能为空"),

    // 分组错误 (PMS_015 ~ PMS_019)
    GROUP_NOT_FOUND("PMS_015", "商品分组不存在"),
    GROUP_NAME_EXISTS("PMS_016", "分组名称已存在，请更换"),
    GROUP_SYSTEM_CANNOT_DELETE("PMS_017", "系统内置分组不允许删除"),

    // 登录错误 (PMS_018 ~ PMS_019)
    LOGIN_REQUIRED("PMS_018", "请先登录"),

    // 供应商错误 (PMS_020 ~ PMS_029)
    SUPPLIER_NOT_FOUND("PMS_020", "供应商不存在"),
    SUPPLIER_CLOSED("PMS_021", "供应商已关闭"),
    SUPPLIER_AUDIT_FAILED("PMS_022", "供应商审核失败"),
    SUPPLIER_ID_EMPTY("PMS_023", "供应商ID不能为空"),
    SUPPLIER_AUDIT_STATUS_INVALID("PMS_024", "只有待审核状态的供应商才能进行审核"),

    // 结算错误 (PMS_025 ~ PMS_029)
    SETTLEMENT_NOT_FOUND("PMS_025", "结算信息不存在"),
    SETTLEMENT_ALREADY_EXISTS("PMS_026", "供应商结算信息已存在"),
    SETTLEMENT_PROCESS_FAILED("PMS_027", "处理订单结算消息失败"),

    // 价格错误 (PMS_030 ~ PMS_031)
    PRICE_CALC_FAILED("PMS_030", "价格计算失败"),

    // 扣款错误 (PMS_032 ~ PMS_035)
    DEDUCTION_RECORD_NOT_FOUND("PMS_032", "扣款记录不存在"),
    DEDUCTION_AMOUNT_INVALID("PMS_033", "扣款金额必须大于0"),
    DEDUCTION_CREATE_FAILED("PMS_034", "创建扣款记录失败"),
    DEDUCTION_FAILED("PMS_035", "扣款失败"),

    // 资产/余额错误 (PMS_036 ~ PMS_039)
    TRANSACTION_AMOUNT_INVALID("PMS_036", "交易金额必须大于0"),
    BALANCE_INCREASE_FAILED("PMS_037", "增加余额失败"),
    BALANCE_INSUFFICIENT("PMS_038", "可用余额不足"),
    BALANCE_DECREASE_FAILED("PMS_039", "减少余额失败，可能余额不足"),

    // 运费错误 (PMS_040 ~ PMS_049)
    FREIGHT_NOT_FOUND("PMS_040", "运费信息不存在"),
    FREIGHT_TEMPLATE_NOT_EXIST("PMS_041", "运费模板不存在"),

    // 品牌错误 (PMS_050 ~ PMS_059)
    BRAND_NOT_FOUND("PMS_050", "品牌不存在"),

    // 通用操作错误 (PMS_060 ~ PMS_069)
    OPERATION_FAILED("PMS_060", "操作失败"),
    DATA_NOT_FOUND("PMS_061", "数据不存在"),
    PARAM_INVALID("PMS_062", "参数非法"),
    STOCK_INSUFFICIENT("PMS_063", "库存不足"),
    RECORD_ID_EMPTY("PMS_064", "记录ID不能为空"),
    ASSET_INIT_FAILED("PMS_065", "初始化供应商资产账户失败"),

    // 提现错误 (PMS_070 ~ PMS_084)
    WITHDRAWAL_ID_EMPTY("PMS_070", "提现ID不能为空"),
    WITHDRAWAL_AMOUNT_INVALID("PMS_071", "提现金额必须大于0"),
    WITHDRAWAL_AMOUNT_BELOW_MIN("PMS_072", "提现金额不能低于{0}元"),
    WITHDRAWAL_AMOUNT_EXCEED_MAX("PMS_073", "提现金额不能超过{0}元"),
    WITHDRAWAL_PENDING_DEDUCTION("PMS_074", "存在未扣款记录，无法发起提现"),
    WITHDRAWAL_BALANCE_INSUFFICIENT("PMS_075", "可提现余额不足，当前可提现余额为{0}元"),
    SETTLEMENT_ACCOUNT_INCOMPLETE("PMS_076", "请先完善结算账户信息"),
    BANK_ACCOUNT_INCOMPLETE("PMS_077", "银行账户信息不完整"),
    WITHDRAWAL_APPLY_FAILED("PMS_078", "提现申请失败，请稍后重试"),
    WITHDRAWAL_DEDUCT_FAILED("PMS_079", "扣减提现金额失败"),
    WITHDRAWAL_REFUND_FAILED("PMS_080", "返还提现金额失败"),
    WITHDRAWAL_RECORD_NOT_FOUND("PMS_081", "提现记录不存在"),
    WITHDRAWAL_AUDIT_STATUS_INVALID("PMS_082", "只能审核待审核状态的提现申请"),
    WITHDRAWAL_CONFIRM_STATUS_INVALID("PMS_083", "只能确认打款中或审核通过状态的提现申请"),
    WITHDRAWAL_MARK_FAILED_STATUS_INVALID("PMS_084", "只能标记打款中或审核通过状态的提现申请为失败"),

    // 审核错误 (PMS_085 ~ PMS_089)
    AUDIT_STATUS_EMPTY("PMS_085", "审核状态不能为空"),
    AUDIT_STATUS_INVALID("PMS_086", "无效的审核状态"),
    REJECT_REASON_EMPTY("PMS_087", "拒绝原因不能为空"),
    TRANSACTION_NO_EMPTY("PMS_088", "交易流水号不能为空"),
    FAIL_REASON_EMPTY("PMS_089", "失败原因不能为空"),

    // 查询错误 (PMS_090 ~ PMS_099)
    QUERY_FAILED("PMS_090", "查询失败"),
    DETAIL_QUERY_FAILED("PMS_091", "详情查询失败"),

    // 店铺错误 (PMS_092 ~ PMS_099)
    SHOP_NOT_FOUND("PMS_092", "店铺不存在"),
    SHOP_DISABLED("PMS_093", "店铺已关闭"),
    SHOP_FAVOR_ALREADY("PMS_094", "该店铺已收藏"),

    // 系统保留错误码 (PMS_900 ~ PMS_999)
    SYSTEM_ERROR("PMS_900", "系统错误"),
    UNKNOWN_ERROR("PMS_999", "未知错误");

    private final String code;
    private final String msg;

    PmsResultCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
