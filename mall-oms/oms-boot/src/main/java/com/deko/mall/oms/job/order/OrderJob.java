package com.deko.mall.oms.job.order;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.deko.mall.oms.enums.OrderStatusEnum;
import com.deko.mall.oms.model.query.OrderPageQuery;
import com.deko.mall.oms.model.vo.OrderPageVO;
import com.deko.mall.oms.service.order.OrderFacadeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 订单定时任务
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OrderJob {

    private final OrderFacadeService orderFacadeService;

    /**
     * 订单完成后等待结算的天数
     */
    @Value("${xyz.mall.receive.wait-days:10}")
    private int receiveWaitDays;


    /**
     * 订单定时变更已完成
     */
    @Scheduled(cron = "0 0/2 * * * *")
    public void orderSuccess() {
        log.info("--------订单定时变更为已完成job------");
        OrderPageQuery queryParams = new OrderPageQuery();
        //查询发货时间14天前的订单
        queryParams.setDeliveryEndDate(LocalDateTime.now().minusDays(receiveWaitDays));
        queryParams.setStatus(Collections.singletonList(OrderStatusEnum.DELIVERED.getValue()));
        while (true) {
            IPage<OrderPageVO> orderPage = orderFacadeService.getOrderPage(queryParams);
            if (orderPage.getRecords().isEmpty()) {
                return;
            }
            for (OrderPageVO vo : orderPage.getRecords()) {
                log.info("变更为已完成orderNo:{}", vo.getOrderNo());
                orderFacadeService.confirmOrder(vo.getOrderNo());
            }
            int pageNum = queryParams.getPageNum();
            pageNum++;
            queryParams.setPageNum(pageNum);
        }

    }


    /**
     * 订单定时变更已取消
     */
    @Scheduled(cron = "0 0/2 * * * *")
    public void orderClose() {
        log.info("--------订单定时变更为已取消job------");
        OrderPageQuery queryParams = new OrderPageQuery();
        queryParams.setStatus(Collections.singletonList(OrderStatusEnum.UNPAID.getValue()));
        while (true) {
            //结束时间设为当前时间前30分钟
            queryParams.setEndDate(LocalDateTime.now().minusMinutes(30));
            queryParams.setStatus(Collections.singletonList(OrderStatusEnum.UNPAID.getValue()));
            IPage<OrderPageVO> orderPage = orderFacadeService.getOrderPage(queryParams);

            if (orderPage.getRecords().isEmpty()) {
                return;
            }
            for (OrderPageVO vo : orderPage.getRecords()) {
                orderFacadeService.cancelOrder(vo.getOrderNo());

            }
            int pageNum = queryParams.getPageNum();
            pageNum++;
            queryParams.setPageNum(pageNum);
        }
    }

}
