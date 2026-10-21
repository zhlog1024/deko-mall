package com.deko.mall.common.rabbitmq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 系统通知消息体（通过 RabbitMQ 传递）
 * <p>
 * 各业务服务发送此消息到 system.notify.exchange，
 * mall-system 消费后根据 templateCode 渲染模板并通过对应渠道发送。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemNotifyMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息模板编码（对应 sys_msg_template.template_code）
     */
    private String templateCode;

    /**
     * 模板参数
     */
    private Map<String, Object> params;

    /**
     * 业务ID（如提现单号、订单号等，用于追溯）
     */
    private String businessId;

    /**
     * 消息来源服务标识（如 PMS_SERVICE, UMS_SERVICE）
     */
    private String source;

}
