package com.deko.mall.oms.service.order.statemachine;

import com.deko.mall.oms.enums.OrderItemStatusEnum;
import com.deko.mall.oms.model.entity.order.OmsOrderItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 订单商品项状态机
 * <p>
 * 商品项为履约/售后承载单元，状态独立流转：
 * 1-待发货 → 2-已发货 → 3-已收货
 * 1/2/3 → 4-售后中 → 5-已退款 或 4 → 恢复 pre_status（售后拒绝/取消/部分退款）
 * 整单取消：1 → 0（由取消链路直接置位，不经本状态机）
 */
@Slf4j
@Component
public class OrderItemStateMachine {

    /**
     * 商品项状态流转校验
     *
     * @param item     商品项
     * @param toStatus 目标状态
     * @return 是否可以流转
     */
    public boolean canTransform(OmsOrderItem item, OrderItemStatusEnum toStatus) {
        OrderItemStatusEnum fromStatus = OrderItemStatusEnum.getByValue(item.getStatus());
        if (fromStatus == null) {
            return false;
        }
        return switch (fromStatus) {
            case PENDING_SHIP -> toStatus == OrderItemStatusEnum.SHIPPED
                    || toStatus == OrderItemStatusEnum.REFUNDING;
            case SHIPPED -> toStatus == OrderItemStatusEnum.RECEIVED
                    || toStatus == OrderItemStatusEnum.REFUNDING;
            case RECEIVED -> toStatus == OrderItemStatusEnum.REFUNDING;
            case REFUNDING -> toStatus == OrderItemStatusEnum.REFUNDED
                    || toStatus == OrderItemStatusEnum.PENDING_SHIP
                    || toStatus == OrderItemStatusEnum.SHIPPED
                    || toStatus == OrderItemStatusEnum.RECEIVED;
            default -> false;
        };
    }

    /**
     * 商品项状态流转
     *
     * @param item     商品项
     * @param toStatus 目标状态
     * @return 是否流转成功
     */
    public boolean transform(OmsOrderItem item, OrderItemStatusEnum toStatus) {
        if (!canTransform(item, toStatus)) {
            log.warn("商品项状态不可流转: itemId={}, fromStatus={}, toStatus={}",
                    item.getId(), item.getStatus(), toStatus.getValue());
            return false;
        }
        item.setStatus(toStatus.getValue());
        return true;
    }
}
