package com.deko.mall.system.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.deko.mall.common.rabbitmq.config.RetryConfig;
import com.deko.mall.common.rabbitmq.constant.MqBizConstants;
import com.deko.mall.common.rabbitmq.dto.SystemNotifyMessage;
import com.deko.mall.common.rabbitmq.listener.AbstractMessageListener;
import com.deko.mall.common.rabbitmq.service.MessageProcessService;
import com.deko.mall.system.service.msg.SysMsgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 系统通知消息监听器
 * <p>
 * 消费各业务服务发送的系统通知消息，通过消息模板+渠道发送（企微、短信等）。
 */
@Component
@Slf4j
public class SystemNotifyListener extends AbstractMessageListener<SystemNotifyMessage> {

    private final SysMsgService sysMsgService;

    public SystemNotifyListener(MessageProcessService messageProcessService,
                                RetryConfig retryConfig,
                                ObjectMapper objectMapper,
                                SysMsgService sysMsgService) {
        super(messageProcessService, retryConfig, objectMapper);
        this.sysMsgService = sysMsgService;
    }

    @RabbitListener(queues = MqBizConstants.SystemNotify.QUEUE)
    @Override
    public void handleMessage(Message message, Channel channel) throws Exception {
        super.handleMessage(message, channel);
    }

    @Override
    protected void processMessage(SystemNotifyMessage notifyMessage) throws Exception {
        log.info("收到系统通知消息: templateCode={}, businessId={}, source={}",
                notifyMessage.getTemplateCode(), notifyMessage.getBusinessId(), notifyMessage.getSource());

        try {
            sysMsgService.sendByTemplate(
                    notifyMessage.getTemplateCode(),
                    notifyMessage.getParams(),
                    notifyMessage.getBusinessId(),
                    notifyMessage.getSource()
            );
            log.info("系统通知消息处理成功: templateCode={}, businessId={}",
                    notifyMessage.getTemplateCode(), notifyMessage.getBusinessId());
        } catch (Exception e) {
            log.error("系统通知消息处理失败: templateCode={}, businessId={}, error={}",
                    notifyMessage.getTemplateCode(), notifyMessage.getBusinessId(), e.getMessage(), e);
            throw e;
        }
    }
}
