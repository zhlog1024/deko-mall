package com.deko.mall.common.rabbitmq.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * 消息重试配置
 */
@Data
@Primary
@Component
@ConfigurationProperties(prefix = "message.retry")
public class RetryConfig {

    /**
     * 是否启用重试
     */
    private boolean enabled = true;

    /**
     * 重试间隔(毫秒)
     */
    private long interval = 60000;

    /**
     * 最大重试次数
     */
    private int maxAttempts = 3;

    /**
     * 间隔时间乘数
     */
    private double multiplier = 2.0;

    /**
     * 最大间隔时间(毫秒)
     */
    private long maxInterval = 5000;

    /**
     * 是否可以重试
     */
    public boolean canRetry(int retryCount) {
        return enabled && retryCount < maxAttempts;
    }

    /**
     * 获取下一次重试间隔
     */
    public long getNextInterval(int retryCount) {
        if (!canRetry(retryCount)) {
            return maxInterval;
        }
        long interval = this.interval * (long) Math.pow(multiplier, retryCount);
        return Math.min(interval, maxInterval);
    }
}
