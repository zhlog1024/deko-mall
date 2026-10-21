package com.deko.mall.system.listener.rabbitmq;

import com.deko.mall.system.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Canal + RabbitMQ 监听数据库数据变化
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CanalListener {

    private final SysMenuService menuService;

    //@RabbitListener(queues = "canal.queue")
    public void handleDataChange() {

    }
}
