package com.deko.mall.pay.config;

import com.deko.mall.common.rabbitmq.constant.MqBizConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 支付模块消息队列配置
 * <p>
 * mall-pay 作为消息生产者，声明 Exchange 和 Queue（确保独立部署时 MQ 资源就绪）。
 * 消费者 mall-oms 侧也会声明（两边都声明是 RabbitMQ 的最佳实践，幂等安全）。
 */
@Configuration
@ComponentScan("com.deko.mall.common.rabbitmq")
public class PayMqConfig {

    // 支付结果消息（pay → oms）
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

    // 退款结果消息（pay → oms）
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
