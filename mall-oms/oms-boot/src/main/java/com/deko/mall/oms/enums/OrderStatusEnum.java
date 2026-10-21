package com.deko.mall.oms.enums;

import com.deko.mall.common.base.IBaseEnum;
import lombok.Getter;

/**
 * 订单状态枚举
 */
public enum OrderStatusEnum implements IBaseEnum<Integer> {

    /**
     * 待付款
     */
    UNPAID(0, "待付款"),
    /**
     * 已付款(待发货)
     */
    PAID(1, "待发货"),
    /**
     * 已发货
     */
    DELIVERED(2, "待收货"),
    /**
     * 已完成
     */
    COMPLETED(3, "已完成"),
    /**
     * 已取消
     */
    CANCELED(4, "已取消"),
    /**
     * 售后中
     */
    REFUNDING(5, "售后中"),

    REFUNDED(6, "已退款");

    @Getter
    private Integer value;
    @Getter
    private String label;

    OrderStatusEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    public static OrderStatusEnum getByValue(Integer status) {
        for (OrderStatusEnum orderStatusEnum : OrderStatusEnum.values()) {
            if (orderStatusEnum.getValue().equals(status)) {
                return orderStatusEnum;
            }
        }
        return null;
    }

    /**
     * 判断订单是否可以退款
     * 已付款、已发货、已完成的订单可以申请退款
     *
     * @param status 订单状态
     * @return true-可以退款 false-不可退款
     */
    public static boolean canRefund(Integer status) {
        OrderStatusEnum orderStatus = getByValue(status);
        if (orderStatus == null) {
            return false;
        }
        return orderStatus == PAID || orderStatus == DELIVERED || orderStatus == COMPLETED;
    }

    /**
     * 判断订单是否可以取消
     * 待付款的订单可以取消
     *
     * @param status 订单状态
     * @return true-可以取消 false-不可取消
     */
    public static boolean canCancel(Integer status) {
        OrderStatusEnum orderStatus = getByValue(status);
        if (orderStatus == null) {
            return false;
        }
        return orderStatus == UNPAID;
    }

    /**
     * 判断订单是否可以删除
     * 已取消、已完成、已退款的订单可以删除
     *
     * @param status 订单状态
     * @return true-可以删除 false-不可删除
     */
    public static boolean canDelete(Integer status) {
        OrderStatusEnum orderStatus = getByValue(status);
        if (orderStatus == null) {
            return false;
        }
        return orderStatus == CANCELED || orderStatus == COMPLETED || orderStatus == REFUNDED;
    }

    /**
     * 判断订单是否可以发货
     * 已付款的订单可以发货
     *
     * @param status 订单状态
     * @return true-可以发货 false-不可发货
     */
    public static boolean canShip(Integer status) {
        OrderStatusEnum orderStatus = getByValue(status);
        if (orderStatus == null) {
            return false;
        }
        return orderStatus == PAID;
    }

    /**
     * 判断订单是否可以完成
     * 已发货的订单可以完成
     *
     * @param status 订单状态
     * @return true-可以完成 false-不可完成
     */
    public static boolean canComplete(Integer status) {
        OrderStatusEnum orderStatus = getByValue(status);
        if (orderStatus == null) {
            return false;
        }
        return orderStatus == DELIVERED;
    }

    /**
     * 判断订单是否处于正常流程中
     * 排除已取消、已退款的订单
     *
     * @param status 订单状态
     * @return true-正常流程 false-非正常流程
     */
    public static boolean isNormalFlow(Integer status) {
        OrderStatusEnum orderStatus = getByValue(status);
        if (orderStatus == null) {
            return false;
        }
        return orderStatus != CANCELED && orderStatus != REFUNDED;
    }

    public static boolean canPay(Integer status) {
        OrderStatusEnum orderStatus = getByValue(status);
        if (orderStatus == null) {
            return false;
        }
        if (orderStatus == UNPAID) {
            return true;
        }
        return false;
    }
}
