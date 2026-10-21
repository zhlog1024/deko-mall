package com.deko.mall.ums.config;

import com.deko.mall.common.rabbitmq.constant.MqBizConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 会员服务消息队列配置
 */
@Configuration
@ComponentScan("com.deko.mall.common.rabbitmq")
public class UmsMqConfig {

    // === 系统通知消息（仅声明交换机，用于发送） ===
    @Bean
    public DirectExchange systemNotifyExchange() {
        return new DirectExchange(MqBizConstants.SystemNotify.EXCHANGE);
    }

}
