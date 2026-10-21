package com.deko.mall.system.service.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 文件存储工厂类
 */
@Slf4j
@Component
public class SysFileStorageFactory {

    private final List<SysFileStorageService> sysFileStorageServices;
    private final Map<String, SysFileStorageService> fileStorageServiceMap;

    public SysFileStorageFactory(List<SysFileStorageService> sysFileStorageServices) {
        this.sysFileStorageServices = sysFileStorageServices;
        this.fileStorageServiceMap = sysFileStorageServices.stream()
                .collect(Collectors.toMap(SysFileStorageService::getChannelCode, Function.identity()));
        log.info("已加载 {} 个文件存储渠道: {}", sysFileStorageServices.size(),
                sysFileStorageServices.stream()
                        .map(SysFileStorageService::getChannelCode)
                        .collect(Collectors.joining(", ")));
    }

    /**
     * 获取文件存储服务
     *
     * @param channelCode 渠道编码
     * @return 文件存储服务
     */
    public SysFileStorageService getFileStorageService(String channelCode) {
        SysFileStorageService service = fileStorageServiceMap.get(channelCode);
        if (service == null) {
            log.warn("未找到渠道编码为 {} 的文件存储服务，将使用默认渠道", channelCode);
            return getDefaultFileStorageService();
        }
        return service;
    }

    /**
     * 获取默认文件存储服务
     *
     * @return 默认文件存储服务
     */
    public SysFileStorageService getDefaultFileStorageService() {
        if (sysFileStorageServices.isEmpty()) {
            throw new IllegalStateException("未配置任何文件存储服务");
        }
        return sysFileStorageServices.get(0);
    }
}
