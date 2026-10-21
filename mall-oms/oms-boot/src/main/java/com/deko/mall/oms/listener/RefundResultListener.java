package com.deko.mall.oms.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.deko.mall.common.rabbitmq.config.RetryConfig;
import com.deko.mall.common.rabbitmq.constant.MqBizConstants;
import com.deko.mall.common.rabbitmq.listener.AbstractMessageListener;
import com.deko.mall.common.rabbitmq.service.MessageProcessService;
import com.deko.mall.pay.api.dto.RefundResultDTO;
import com.deko.mall.oms.service.order.OrderFacadeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 退款结果消息监听器
 * 统一处理支付系统发送的退款结果通知
 */
@Component
@Slf4j
public class RefundResultListener extends AbstractMessageListener<RefundResultDTO> {

    private final OrderFacadeService orderFacadeService;

    public RefundResultListener(MessageProcessService messageProcessService,
                                RetryConfig retryConfig,
                                ObjectMapper objectMapper,
                                OrderFacadeService orderFacadeService) {
        super(messageProcessService, retryConfig, objectMapper);
        this.orderFacadeService = orderFacadeService;
    }


    @RabbitListener(queues = MqBizConstants.RefundResult.QUEUE)
    @Override
    public void handleMessage(Message message, Channel channel) throws Exception {
        // 直接调用父类通用处理方法
        super.handleMessage(message, channel);
    }


    @Override
    protected void processMessage(RefundResultDTO refundResultDTO) throws Exception {

        log.info("收到退款结果通知: refundNo={}, orderNo={}",
                refundResultDTO.getRefundNo(), refundResultDTO.getOrderNo());

        try {
            // 通过OrderService门面处理退款结果
            boolean result = orderFacadeService.handleRefundCallback(refundResultDTO);
            if (result) {
                log.info("退款结果处理成功: refundNo={}", refundResultDTO.getRefundNo());
            } else {
                log.warn("退款结果处理失败: refundNo={}", refundResultDTO.getRefundNo());
            }
        } catch (Exception e) {
            log.error("处理退款结果异常: refundNo={}", refundResultDTO.getRefundNo(), e);
        }


    }

}
