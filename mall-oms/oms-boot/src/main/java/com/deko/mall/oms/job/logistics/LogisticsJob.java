package com.deko.mall.oms.job.logistics;

import com.deko.mall.oms.enums.LogisticsStatusEnum;
import com.deko.mall.oms.model.entity.logistics.OmsLogisticsInfo;
import com.deko.mall.oms.service.logistics.LogisticsInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物流补偿查询定时任务
 * <p>
 * 查询规则：
 * 1. 已签收的订单不再查询
 * 2. 有回调记录且未超时的订单，6小时查询一次
 * 3. 无回调记录或回调超时的订单，2小时查询一次
 * 4. 查询次数超过20次的订单不再查询
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogisticsJob {

    // 回调后查询间隔（分钟）
    private static final int CALLBACK_QUERY_INTERVAL = 360;  // 6小时
    // 普通查询间隔（分钟）
    private static final int NORMAL_QUERY_INTERVAL = 120;    // 2小时
    // 回调超时时间（分钟）
    private static final int CALLBACK_TIMEOUT = 30;
    // 最大查询次数
    private static final int MAX_QUERY_COUNT = 20;
    private final LogisticsInfoService logisticsInfoService;

    /**
     * 每5分钟执行一次补偿查询
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void execute() {
        log.info("开始执行物流补偿查询任务");
        try {
            // 1. 查询未签收的物流订单
            List<OmsLogisticsInfo> orders = logisticsInfoService.getCompensateOrders();
            if (orders.isEmpty()) {
                log.info("无需补偿查询的物流订单");
                return;
            }

            // 2. 执行补偿查询
            LocalDateTime now = LocalDateTime.now();
            int successCount = 0;
            int skipCount = 0;
            for (OmsLogisticsInfo order : orders) {
                try {
                    // 检查是否需要查询
                    if (!needQuery(order, now)) {
                        skipCount++;
                        continue;
                    }

                    boolean success = logisticsInfoService.compensateQueryTrack(order.getTrackingNo());
                    if (success) {
                        successCount++;
                    }
                } catch (Exception e) {
                    log.error("物流补偿查询异常，trackingNo={}", order.getTrackingNo(), e);
                }
            }

            log.info("物流补偿查询任务执行完成，处理订单数：{}，成功数：{}，跳过数：{}",
                    orders.size(), successCount, skipCount);
        } catch (Exception e) {
            log.error("物流补偿查询任务执行异常", e);
        }
    }

    /**
     * 判断是否需要查询
     * 规则：
     * 1. 已签收的订单不再查询
     * 2. 有回调记录且未超时的订单，6小时查询一次
     * 3. 无回调记录或回调超时的订单，2小时查询一次
     * 4. 查询次数超过20次的订单不再查询
     */
    private boolean needQuery(OmsLogisticsInfo logistics, LocalDateTime now) {
        // 已签收不再查询
        if (LogisticsStatusEnum.SIGNED.getValue().equals(logistics.getStatus())) {
            return false;
        }

        // 超过最大查询次数不再查询
        Long count = logistics.getQueryCount();
        if (count != null && count >= MAX_QUERY_COUNT) {
            log.info("物流订单超过最大查询次数，不再查询, trackingNo={}, queryCount={}",
                    logistics.getTrackingNo(), logistics.getQueryCount());
            return false;
        }

        // 判断回调是否有效
        boolean hasValidCallback = logistics.getLastCallbackTime() != null
                && logistics.getLastCallbackTime().plusMinutes(CALLBACK_TIMEOUT).isAfter(now);

        // 获取查询间隔
        int interval = hasValidCallback ? CALLBACK_QUERY_INTERVAL : NORMAL_QUERY_INTERVAL;

        // 获取最后更新时间
        LocalDateTime lastUpdateTime = logistics.getLastQueryTime();
        if (logistics.getLastCallbackTime() != null) {
            lastUpdateTime = lastUpdateTime == null ? logistics.getLastCallbackTime()
                    : lastUpdateTime.isAfter(logistics.getLastCallbackTime()) ? lastUpdateTime
                    : logistics.getLastCallbackTime();
        }

        // 首次查询或距离上次更新超过间隔时间
        return lastUpdateTime == null || lastUpdateTime.plusMinutes(interval).isBefore(now);
    }
}
