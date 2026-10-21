package com.deko.mall.oms.listener;

import com.deko.mall.oms.enums.LogisticsTypeEnum;
import com.deko.mall.oms.model.event.LogisticsSignedEvent;
import com.deko.mall.oms.service.order.OrderFacadeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 物流签收事件监听器
 * <p>
 * 监听 LogisticsSignedEvent，当发货物流已签收时自动确认收货。
 * 使用 Spring 事件机制解耦，避免 LogisticsInfoService ↔ OrderFacadeService 循环依赖。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogisticsSignedListener {

    private final OrderFacadeService orderFacadeService;

    /**
     * 处理物流签收事件：自动确认收货
     * <p>
     * confirmOrder 内置 CAS 保护（WHERE status = DELIVERED），重复调用安全。
     * 仅处理发货类型物流，退货类型由 LogisticsInfoServiceImpl 侧过滤。
     */
    @EventListener
    public void onLogisticsSigned(LogisticsSignedEvent event) {
        if (!LogisticsTypeEnum.DELIVERY.getValue().equals(event.getLogisticsType())) {
            return;
        }

        String orderNo = event.getBizNo();
        log.info("收到物流签收事件，尝试自动确认收货: orderNo={}, trackingNo={}",
                orderNo, event.getTrackingNo());

        try {
            // 按运单号定位发货单，仅确认该发货单关联的商品项（多发货单订单互不影响）
            boolean success = orderFacadeService.confirmOrder(orderNo, event.getTrackingNo());
            if (success) {
                log.info("自动确认收货成功: orderNo={}", orderNo);
            } else {
                log.info("自动确认收货未执行（订单可能已确认或状态不符）: orderNo={}", orderNo);
            }
        } catch (Exception e) {
            log.warn("自动确认收货失败: orderNo={}, error={}", orderNo, e.getMessage());
        }
    }
}
