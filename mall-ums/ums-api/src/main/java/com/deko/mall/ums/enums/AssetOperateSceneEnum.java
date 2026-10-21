package com.deko.mall.ums.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 资产操作类型枚举
 * 统一描述业务场景和操作类型，是资产流水记录的唯一依据
 */
@Getter
@AllArgsConstructor
@Schema(description = "资产操作类型枚举")
public enum AssetOperateSceneEnum {

    // 订单场景
    @Schema(description = "订单下单扣减")
    ORDER_CREATE_DEDUCT("ORDER_CREATE_DEDUCT", "订单下单扣减", "DEDUCT", "扣减"),

    @Schema(description = "订单取消退回")
    ORDER_CANCEL_REFUND("ORDER_CANCEL_REFUND", "订单取消退回", "GRANT", "退回"),

    @Schema(description = "订单退款退回")
    ORDER_REFUND_REFUND("ORDER_REFUND_REFUND", "订单退款退回", "GRANT", "退回"),

    // 收益发放场景
    @Schema(description = "收益发放增加")
    PROFIT_GRANT("PROFIT_GRANT", "收益发放增加", "GRANT", "增加"),

    // 转账场景
    @Schema(description = "转账支出")
    TRANSFER_OUT("TRANSFER_OUT", "转账支出", "DEDUCT", "扣减"),

    @Schema(description = "转账收入")
    TRANSFER_IN("TRANSFER_IN", "转账收入", "GRANT", "发放"),

    // 内部调整场景
    @Schema(description = "系统增加")
    ADJUST_IN("ADJUST_IN", "系统增加", "GRANT", "发放"),

    @Schema(description = "系统扣减")
    ADJUST_OUT("ADJUST_OUT", "系统扣减", "DEDUCT", "扣减");

    @Schema(description = "业务场景类型编码")
    private final String sceneCode;

    @Schema(description = "场景描述")
    private final String sceneDescription;

    @Schema(description = "操作类型（FREEZE-冻结/UNFREEZE-解冻/USE-使用/GRANT-发放/DEDUCT-扣减）")
    private final String operateType;

    @Schema(description = "操作类型描述（中文）")
    private final String typeDescription;

    /**
     * 根据编码获取枚举
     */
    public static AssetOperateSceneEnum fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        for (AssetOperateSceneEnum type : values()) {
            if (type.getSceneCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 是否为冻结操作
     */
    public boolean isFreeze() {
        return "FREEZE".equals(operateType);
    }

    /**
     * 是否为解冻操作
     */
    public boolean isUnfreeze() {
        return "UNFREEZE".equals(operateType);
    }

    /**
     * 是否为使用操作
     */
    public boolean isUse() {
        return "USE".equals(operateType);
    }

    /**
     * 是否为发放操作（收入类）
     */
    public boolean isGrant() {
        return "GRANT".equals(operateType);
    }

    /**
     * 是否为扣减操作（支出类）
     */
    public boolean isDeduct() {
        return "DEDUCT".equals(operateType);
    }

    /**
     * 是否记录流水明细
     */
    public boolean shouldRecordFlow() {
        return true;
    }

    /**
     * 判断是否需要校验冻结金额（已废弃，不再使用冻结）
     */
    public boolean requiresFrozenCheck() {
        return false;
    }

    /**
     * 判断是否为转账相关操作
     */
    public boolean isTransfer() {
        return this == TRANSFER_OUT || this == TRANSFER_IN;
    }
}
