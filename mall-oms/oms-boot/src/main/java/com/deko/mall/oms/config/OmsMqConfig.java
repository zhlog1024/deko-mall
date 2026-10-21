package com.deko.mall.oms.config;

import com.deko.mall.common.rabbitmq.constant.MqBizConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 订单模块消息队列配置
 */
@Configuration
@ComponentScan("com.deko.mall.common.rabbitmq")
public class OmsMqConfig {

    // 支付成功消息
    @Bean
    public DirectExchange paymentResultExchange() {
        return new DirectExchange(MqBizConstants.PaymentResult.EXCHANGE);
    }

    @Bean
    public Queue paymentResultQueue() {
        return new Queue(MqBizConstants.PaymentResult.QUEUE);
    }

    @Bean
    public Binding paymentResultBinding() {
        return BindingBuilder.bind(paymentResultQueue())
                .to(paymentResultExchange())
                .with(MqBizConstants.PaymentResult.ROUTING_KEY);
    }


    // === 退款结果相关配置 ===
    @Bean
    public DirectExchange refundResultExchange() {
        return new DirectExchange(MqBizConstants.RefundResult.EXCHANGE);
    }

    @Bean
    public Queue refundResultQueue() {
        return new Queue(MqBizConstants.RefundResult.QUEUE);
    }

    @Bean
    public Binding refundResultBinding() {
        return BindingBuilder.bind(refundResultQueue())
                .to(refundResultExchange())
                .with(MqBizConstants.RefundResult.ROUTING_KEY);
    }

}
