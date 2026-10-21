package com.deko.mall.common.rabbitmq.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ配置属性
 */
@Data
@Primary
@Component
@ConfigurationProperties(prefix = "spring.rabbitmq.listener")
public class RabbitProperties {

    /**
     * 并发消费者数量
     */
    private Integer concurrentConsumers = 1;

    /**
     * 最大并发消费者数量
     */
    private Integer maxConcurrentConsumers = 1;

    /**
     * 预取数量
     */
    private Integer prefetchCount = 1;
}
