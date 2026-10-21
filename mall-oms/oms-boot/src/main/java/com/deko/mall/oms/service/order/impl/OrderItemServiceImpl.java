package com.deko.mall.oms.service.order.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.pay.api.enums.RefundStatusEnum;
import com.deko.mall.oms.mapper.OrderItemMapper;
import com.deko.mall.oms.model.entity.order.OmsOrderItem;
import com.deko.mall.oms.service.order.OrderItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 订单商品服务实现类
 */
@Slf4j
@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OmsOrderItem> implements OrderItemService {

    /**
     * 将订单项标记为已全额退款（全部数量退款）
     *
     * @param orderItemId 订单项ID
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateItemFullyRefunded(Long orderItemId) {
        log.info("将订单商品标记为已全额退款: orderItemId={}", orderItemId);

        // 获取订单项
        OmsOrderItem orderItem = this.getById(orderItemId);
        if (orderItem == null) {
            log.warn("订单项不存在: orderItemId={}", orderItemId);
            return false;
        }

        // 检查是否已经全额退款
        Integer quantity = orderItem.getQuantity();
        Integer refundQuantity = orderItem.getRefundQuantity();
        if (refundQuantity != null && refundQuantity.equals(quantity)) {
            log.info("订单项已经是全额退款状态: orderItemId={}", orderItemId);
            return true;
        }

        // 更新退款状态、数量和金额
        orderItem.setRefundStatus(RefundStatusEnum.REFUND_SUCCESS.getValue());
        orderItem.setRefundQuantity(quantity); // 全部数量
        orderItem.setRefundAmount(orderItem.getTotalAmount()); // 全部金额

        // 更新到数据库
        boolean result = this.updateById(orderItem);
        if (!result) {
            log.error("更新订单项退款信息失败: orderItemId={}", orderItemId);
        } else {
            log.info("订单项已成功标记为全额退款: orderItemId={}, quantity={}, amount={}",
                    orderItemId, quantity, orderItem.getTotalAmount());
        }
        return result;
    }

    /**
     * 检查订单所有商品是否全部退款
     *
     * @param orderId 订单ID
     * @return 是否全部退款完成
     */
    @Override
    public boolean checkAllItemsRefunded(Long orderId) {
        log.info("检查订单商品是否全部已退款: orderId={}", orderId);

        // 获取订单商品列表
        List<OmsOrderItem> orderItems = this.lambdaQuery()
                .eq(OmsOrderItem::getOrderId, orderId)
                .eq(OmsOrderItem::getDeleted, 0)
                .list();

        if (orderItems == null || orderItems.isEmpty()) {
            log.warn("订单商品为空: orderId={}", orderId);
            return false;
        }

        // 只检查退款状态，不检查具体数量
        for (OmsOrderItem item : orderItems) {
            // refundStatus=2 表示退款成功 (RefundStatusEnum.REFUND_SUCCESS)
            Integer refundStatus = item.getRefundStatus() == null ? 0 : item.getRefundStatus();

            // 只要有一个商品未处于退款成功状态，就返回false
            if (refundStatus != RefundStatusEnum.REFUND_SUCCESS.getValue()) {
                log.info("订单部分商品未完全退款: orderId={}, itemId={}, refundStatus={}",
                        orderId, item.getId(), refundStatus);
                return false;
            }
        }

        log.info("订单所有商品已全部退款: orderId={}", orderId);
        return true;
    }
}
