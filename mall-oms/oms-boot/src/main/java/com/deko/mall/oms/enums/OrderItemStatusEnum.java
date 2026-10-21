package com.deko.mall.oms.enums;

import com.deko.mall.common.base.IBaseEnum;
import lombok.Getter;

/**
 * 订单商品项履约状态枚举
 * <p>
 * 商品项为履约/售后承载单元，状态独立于订单流转：
 * 1-待发货 → 2-已发货 → 3-已收货；1/2/3 → 4-售后中 → 5-已退款；整单取消 → 0
 */
@Getter
public enum OrderItemStatusEnum implements IBaseEnum<Integer> {

    /**
     * 已取消（整单取消时全部商品项置 0）
     */
    CANCELED(0, "已取消"),

    /**
     * 待发货（下单后默认态，已付款未发货）
     */
    PENDING_SHIP(1, "待发货"),

    /**
     * 已发货（本供应商商品项全量发货）
     */
    SHIPPED(2, "已发货"),

    /**
     * 已收货（物流签收/确认收货）
     */
    RECEIVED(3, "已收货"),

    /**
     * 售后中（售后申请通过后进入）
     */
    REFUNDING(4, "售后中"),

    /**
     * 已退款（全额退款成功）
     */
    REFUNDED(5, "已退款");

    private final Integer value;
    private final String label;

    OrderItemStatusEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    public static OrderItemStatusEnum getByValue(Integer status) {
        for (OrderItemStatusEnum itemStatus : values()) {
            if (itemStatus.getValue().equals(status)) {
                return itemStatus;
            }
        }
        return null;
    }
}
