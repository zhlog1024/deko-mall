package com.deko.mall.pay.job;

import com.deko.mall.pay.entity.PayPaymentChannel;
import com.deko.mall.pay.service.pay.PayService;
import com.deko.mall.pay.service.pay.PaymentChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class PayJob {

    private final PayService payService;

    private final PaymentChannelService paymentChannelService;


    /**
     * 定时关闭超时未支付的订单
     * 每分钟执行一次
     */
    @Scheduled(cron = "0 */20 * * * ?")
    public void closeExpiredPayments() {
        log.info("开始检查超时未支付订单...");
        List<PayPaymentChannel> expiredRecords = paymentChannelService.getExpiredRecords(LocalDateTime.now());

        for (PayPaymentChannel record : expiredRecords) {
            try {
                // 关闭支付单
                payService.closePayment(record.getPaymentChannelNo());
            } catch (Exception e) {
                log.error("关闭超时支付单异常: " + record.getPaymentChannelNo(), e);
            }
        }
    }

}
