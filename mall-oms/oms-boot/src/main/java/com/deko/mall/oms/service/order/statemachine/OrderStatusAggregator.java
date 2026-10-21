package com.deko.mall.oms.service.order.statemachine;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.deko.mall.oms.enums.OrderItemStatusEnum;
import com.deko.mall.oms.enums.OrderStatusEnum;
import com.deko.mall.oms.model.entity.order.OmsOrder;
import com.deko.mall.oms.model.entity.order.OmsOrderItem;
import com.deko.mall.oms.service.order.OrderItemService;
import com.deko.mall.oms.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 订单状态聚合器
 * <p>
 * 订单级状态（履约/售后/退款段）由商品项状态聚合派生，是本类提供的唯一订单级状态写入口。
 * 支付/取消/关闭仍为订单级真值（不经过聚合）。
 * <p>
 * 聚合规则（已退款商品项为终态，不参与履约聚合）：
 * 1. 任一 item=4 售后中                → 订单 REFUNDING(5)
 * 2. 全部 item=5 已退款                → 订单 REFUNDED(6)
 * 3. 未退款 item 全部=3 已收货          → 订单 COMPLETED(3)（含 {3,5} 混合=部分退款已完成）
 * 4. 未退款 item 全部∈{2,3}             → 订单 DELIVERED(2)（含 {2,3,5} 混合=部分签收）
 * 5. 未退款 item 任一=1 待发货          → 订单 PAID(1)（含 {1,2} 混合=部分发货）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderStatusAggregator {

    private final OrderItemService orderItemService;
    private final OrderMapper orderMapper;

    /**
     * 按商品项状态刷新订单状态（CAS 更新，失败重读重算）
     *
     * @param orderId 订单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void refreshOrderStatus(Long orderId) {
        for (int attempt = 0; attempt < 3; attempt++) {
            OmsOrder order = orderMapper.selectById(orderId);
            if (order == null || !isFulfillmentSegment(order.getStatus())) {
                return;
            }
            List<OmsOrderItem> items = orderItemService.lambdaQuery()
                    .eq(OmsOrderItem::getOrderId, orderId)
                    .eq(OmsOrderItem::getDeleted, 0)
                    .list();
            if (items == null || items.isEmpty()) {
                return;
            }
            OrderStatusEnum target = aggregate(items);
            if (target == null || target.getValue().equals(order.getStatus())) {
                return;
            }
            // CAS：仅当订单仍处于聚合前状态时更新，避免覆盖并发事件
            int affected = orderMapper.update(null,
                    new LambdaUpdateWrapper<OmsOrder>()
                            .eq(OmsOrder::getId, orderId)
                            .eq(OmsOrder::getStatus, order.getStatus())
                            .set(OmsOrder::getStatus, target.getValue()));
            boolean updated = affected > 0;
            if (updated) {
                log.info("订单状态聚合刷新: orderId={}, orderNo={}, {} → {}", orderId, order.getOrderNo(),
                        order.getStatus(), target.getValue());
                return;
            }
            log.debug("订单状态聚合CAS冲突，重试: orderId={}, attempt={}", orderId, attempt + 1);
        }
        log.warn("订单状态聚合CAS重试耗尽: orderId={}", orderId);
    }

    /**
     * 是否处于履约段状态（PAID/DELIVERED/COMPLETED/REFUNDING/REFUNDED 可聚合）
     */
    private boolean isFulfillmentSegment(Integer status) {
        return status != null
                && status >= OrderStatusEnum.PAID.getValue()
                && status <= OrderStatusEnum.REFUNDED.getValue();
    }

    /**
     * 聚合规则
     *
     * @return 目标订单状态；不满足任何规则返回 null
     */
    private OrderStatusEnum aggregate(List<OmsOrderItem> items) {
        long refunding = items.stream().filter(i -> OrderItemStatusEnum.REFUNDING.getValue().equals(i.getStatus())).count();
        long refunded = items.stream().filter(i -> OrderItemStatusEnum.REFUNDED.getValue().equals(i.getStatus())).count();
        int size = items.size();

        if (refunding > 0) {
            return OrderStatusEnum.REFUNDING;
        }
        if (refunded == size) {
            return OrderStatusEnum.REFUNDED;
        }
        // 未退款 item 参与履约聚合
        boolean anyPendingShip = items.stream()
                .filter(i -> !OrderItemStatusEnum.REFUNDED.getValue().equals(i.getStatus()))
                .anyMatch(i -> OrderItemStatusEnum.PENDING_SHIP.getValue().equals(i.getStatus()));
        if (anyPendingShip) {
            return OrderStatusEnum.PAID;
        }
        boolean anyShipped = items.stream()
                .filter(i -> !OrderItemStatusEnum.REFUNDED.getValue().equals(i.getStatus()))
                .anyMatch(i -> OrderItemStatusEnum.SHIPPED.getValue().equals(i.getStatus()));
        if (anyShipped) {
            return OrderStatusEnum.DELIVERED;
        }
        return OrderStatusEnum.COMPLETED;
    }
}
