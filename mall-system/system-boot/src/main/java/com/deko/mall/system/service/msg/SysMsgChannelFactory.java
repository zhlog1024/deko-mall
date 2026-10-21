package com.deko.mall.system.service.msg;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 消息渠道工厂类
 */
@Slf4j
@Component
public class SysMsgChannelFactory {

    private final List<SysMsgChannelService> sysMsgChannelServices;
    private final Map<String, SysMsgChannelService> msgChannelServiceMap;

    public SysMsgChannelFactory(List<SysMsgChannelService> sysMsgChannelServices) {
        this.sysMsgChannelServices = sysMsgChannelServices;
        this.msgChannelServiceMap = sysMsgChannelServices.stream()
                .collect(Collectors.toMap(SysMsgChannelService::getChannelCode, Function.identity()));
        log.info("已加载 {} 个消息渠道: {}", sysMsgChannelServices.size(),
                sysMsgChannelServices.stream()
                        .map(SysMsgChannelService::getChannelCode)
                        .collect(Collectors.joining(", ")));
    }

    /**
     * 获取消息渠道服务
     *
     * @param channelCode 渠道编码
     * @return 消息渠道服务
     */
    public SysMsgChannelService getMsgChannelService(String channelCode) {
        SysMsgChannelService service = msgChannelServiceMap.get(channelCode);
        if (service == null) {
            log.warn("未找到渠道编码为 {} 的消息渠道服务，将使用默认渠道", channelCode);
            return getDefaultMsgChannelService();
        }
        return service;
    }

    /**
     * 获取默认消息渠道服务
     *
     * @return 默认消息渠道服务
     */
    public SysMsgChannelService getDefaultMsgChannelService() {
        if (sysMsgChannelServices.isEmpty()) {
            throw new IllegalStateException("未配置任何消息渠道服务");
        }
        return sysMsgChannelServices.get(0);
    }
}
