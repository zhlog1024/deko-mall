package com.deko.mall.oms.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.deko.mall.common.rabbitmq.config.RetryConfig;
import com.deko.mall.common.rabbitmq.constant.MqBizConstants;
import com.deko.mall.common.rabbitmq.listener.AbstractMessageListener;
import com.deko.mall.common.rabbitmq.service.MessageProcessService;
import com.deko.mall.pay.api.dto.PaymentResultDTO;
import com.deko.mall.oms.model.entity.order.OmsOrder;
import com.deko.mall.oms.service.order.OrderFacadeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 订单支付成功消息监听器
 */
@Component
@Slf4j
public class PaymentResultListener extends AbstractMessageListener<PaymentResultDTO> {

    private final OrderFacadeService orderFacadeService;

    public PaymentResultListener(MessageProcessService messageProcessService,
                                 RetryConfig retryConfig,
                                 ObjectMapper objectMapper,
                                 OrderFacadeService orderFacadeService) {
        super(messageProcessService, retryConfig, objectMapper);
        this.orderFacadeService = orderFacadeService;
    }

    @RabbitListener(queues = MqBizConstants.PaymentResult.QUEUE)
    @Override
    public void handleMessage(Message message, Channel channel) throws Exception {
        // 直接调用父类通用处理方法
        super.handleMessage(message, channel);
    }

    @Override
    protected void processMessage(PaymentResultDTO paymentResult) throws Exception {
        log.info("处理支付成功消息: paymentNo={}, channelTradeNo={}, status={}",
                paymentResult.getPaymentNo(), paymentResult.getChannelTradeNo(), paymentResult.getStatus());

        try {
            // 根据支付单号查询所有相关的订单
            List<OmsOrder> orders = orderFacadeService.lambdaQuery()
                    .eq(OmsOrder::getPaymentNo, paymentResult.getPaymentNo())
                    .list();

            if (orders.isEmpty()) {
                log.error("未找到支付单对应的订单: paymentNo={}", paymentResult.getPaymentNo());
                throw new RuntimeException("未找到支付单对应的订单: " + paymentResult.getPaymentNo());
            }

            log.info("找到{}个订单需要处理支付成功: paymentNo={}, payChannel={}, mchId={}",
                    orders.size(), paymentResult.getPaymentNo(),
                    paymentResult.getPayChannel(), paymentResult.getMchId());

            // 处理每个订单的支付成功逻辑
            for (OmsOrder order : orders) {
                try {
                    // 设置支付相关信息到订单对象
                    order.setPayChannel(paymentResult.getPayChannel());
                    order.setMchId(paymentResult.getMchId());
                    order.setPaymentTime(paymentResult.getPayTime()); // 使用支付回调中的支付时间
                    order.setPaymentChannelNo(paymentResult.getPaymentChannelNo());
                    order.setChannelTradeNo(paymentResult.getChannelTradeNo()); // 冗余渠道交易号，用于对账
                    order.setChannelUserId(paymentResult.getChannelUserId()); // 渠道用户标识，用于发货报备

                    log.info("处理订单支付成功: orderNo={}, payChannel={}, mchId={}, channelTradeNo={}",
                            order.getOrderNo(), order.getPayChannel(), order.getMchId(), order.getChannelTradeNo());
                    orderFacadeService.paymentResult(order);
                    log.info("订单支付成功处理完成: orderNo={}", order.getOrderNo());
                } catch (Exception orderEx) {
                    log.error("订单支付回调处理异常: orderNo={}, 异常信息: {}",
                            order.getOrderNo(), orderEx.getMessage(), orderEx);

                    // 单个订单处理失败，发起快速退款
                    boolean refundResult = orderFacadeService.processQuickRefund(order.getOrderNo());
                    if (refundResult) {
                        log.info("订单支付回调失败，快速退款请求已发送: orderNo={}", order.getOrderNo());
                    } else {
                        log.error("订单支付回调失败，快速退款请求发送失败: orderNo={}", order.getOrderNo());
                    }
                    // 对于单个订单失败，继续处理其他订单
                }
            }

            log.info("支付成功消息处理完成: paymentNo={}, 处理订单数量={}",
                    paymentResult.getPaymentNo(), orders.size());

        } catch (Exception e) {
            log.error("支付成功消息处理异常: paymentNo={}, 异常信息: {}",
                    paymentResult.getPaymentNo(), e.getMessage(), e);
            throw e; // 重新抛出异常，让基类处理重试逻辑
        }
    }
}