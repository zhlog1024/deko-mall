package com.deko.mall.oms.service.order.statemachine;

import com.deko.mall.oms.enums.OrderStatusEnum;
import com.deko.mall.oms.model.entity.order.OmsOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 订单状态机
 * 统一管理订单状态流转
 */
@Slf4j
@Component
public class OrderStateMachine {

    /**
     * 订单状态流转校验
     *
     * @param order    订单
     * @param toStatus 目标状态
     * @return 是否可以流转
     */
    public boolean canTransform(OmsOrder order, OrderStatusEnum toStatus) {
        OrderStatusEnum fromStatus = OrderStatusEnum.getByValue(order.getStatus());
        if (fromStatus == null) {
            return false;
        }

        return switch (fromStatus) {
            case UNPAID -> toStatus == OrderStatusEnum.PAID || toStatus == OrderStatusEnum.CANCELED;
            case PAID -> toStatus == OrderStatusEnum.DELIVERED || toStatus == OrderStatusEnum.REFUNDING;
            case DELIVERED -> toStatus == OrderStatusEnum.COMPLETED || toStatus == OrderStatusEnum.REFUNDING;
            case COMPLETED -> toStatus == OrderStatusEnum.REFUNDING;
            case REFUNDING -> toStatus == OrderStatusEnum.REFUNDED;
            default -> false;
        };
    }

    /**
     * 订单状态流转
     *
     * @param order    订单
     * @param toStatus 目标状态
     * @return 是否流转成功
     */
    public boolean transform(OmsOrder order, OrderStatusEnum toStatus) {
        if (!canTransform(order, toStatus)) {
            log.warn("订单状态不可流转: orderId={}, fromStatus={}, toStatus={}",
                    order.getId(), order.getStatus(), toStatus.getValue());
            return false;
        }

        order.setStatus(toStatus.getValue());
        return true;
    }
}
