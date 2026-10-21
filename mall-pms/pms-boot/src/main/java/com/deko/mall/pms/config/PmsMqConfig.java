package com.deko.mall.pms.config;

import com.deko.mall.common.rabbitmq.constant.MqBizConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 商品模块消息队列配置
 */
@Configuration
@ComponentScan("com.deko.mall.common.rabbitmq")
public class PmsMqConfig {

    /**
     * 供应商结算消息交换机
     */

    /**
     * 供应商结算消息队列
     */



    // 系统通知仅声明交换机，用于发送消息。
    @Bean
    public DirectExchange systemNotifyExchange() {
        return new DirectExchange(MqBizConstants.SystemNotify.EXCHANGE);
    }

}
