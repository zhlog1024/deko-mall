package com.deko.mall.oms.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 物流异步订阅配置
 */
@Configuration
@EnableAsync
@Slf4j
public class LogisticsAsyncConfig {

    @Bean("logisticsAsyncExecutor")
    public Executor logisticsAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("logistics-async-");
        executor.setRejectedExecutionHandler((r, e) ->
                log.warn("物流异步任务队列已满，任务被拒绝: {}", r.toString()));
        executor.initialize();
        return executor;
    }
}

