package com.deko.mall.system.config;

import com.deko.mall.common.rabbitmq.constant.MqBizConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 系统服务消息队列配置
 */
@Configuration
@ComponentScan("com.deko.mall.common.rabbitmq")
public class SystemMqConfig {

    /**
     * 系统通知消息（企微推送等）
     */
    @Bean
    public DirectExchange systemNotifyExchange() {
        return new DirectExchange(MqBizConstants.SystemNotify.EXCHANGE);
    }

    @Bean
    public Queue systemNotifyQueue() {
        return new Queue(MqBizConstants.SystemNotify.QUEUE);
    }

    @Bean
    public Binding systemNotifyBinding() {
        return BindingBuilder.bind(systemNotifyQueue())
                .to(systemNotifyExchange())
                .with(MqBizConstants.SystemNotify.ROUTING_KEY);
    }

}
