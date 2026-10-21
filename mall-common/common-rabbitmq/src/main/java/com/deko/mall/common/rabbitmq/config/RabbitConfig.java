package com.deko.mall.common.rabbitmq.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * RabbitMQ配置类
 * 统一消息转换机制，确保发送和接收消息时一致的序列化和反序列化
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@Import({RabbitProperties.class, RetryConfig.class})
public class RabbitConfig {

    private final ConnectionFactory connectionFactory;
    private final RabbitProperties rabbitProperties;
    private final RetryConfig retryConfig;
    private final ObjectMapper objectMapper;


    /**
     * 消息转换器
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    /**
     * 重试模板配置
     */
    @Bean
    public RetryTemplate retryTemplate() {
        if (!retryConfig.isEnabled()) {
            return null;
        }
        RetryTemplate template = new RetryTemplate();

        // 指数退避策略
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(retryConfig.getInterval());
        backOffPolicy.setMaxInterval(retryConfig.getMaxInterval());
        backOffPolicy.setMultiplier(retryConfig.getMultiplier());
        template.setBackOffPolicy(backOffPolicy);

        // 重试策略
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(retryConfig.getMaxAttempts());
        template.setRetryPolicy(retryPolicy);

        return template;
    }

    /**
     * RabbitTemplate配置
     */
    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());

        // 启用重试
        RetryTemplate retryTemplate = retryTemplate();
        if (retryTemplate != null) {
            template.setRetryTemplate(retryTemplate);
        }

        return template;
    }

    /**
     * 消费者监听工厂配置
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();

        // 基础配置
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);

        // 消费者配置
        factory.setConcurrentConsumers(rabbitProperties.getConcurrentConsumers());
        factory.setMaxConcurrentConsumers(rabbitProperties.getMaxConcurrentConsumers());
        factory.setPrefetchCount(rabbitProperties.getPrefetchCount());

        // 重试配置
        RetryTemplate retryTemplate = retryTemplate();
        if (retryTemplate != null) {
            factory.setRetryTemplate(retryTemplate);
        }

        return factory;
    }
}
