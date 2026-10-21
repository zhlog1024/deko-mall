package com.deko.mall.system.api;

import com.deko.mall.common.result.Result;
import com.deko.mall.common.web.config.FeignDecoderConfig;
import com.deko.mall.system.dto.SysAppConfigValueDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * APP 配置（sys_app_config）Feign 客户端
 * 供 DIY 装修器等跨服务读写单条配置（读原值 / 写值并清理 APP_CONFIG 缓存）
 */
@FeignClient(value = "mall-system", contextId = "sysAppConfig", configuration = {FeignDecoderConfig.class})
public interface SysAppConfigFeignClient {

    /**
     * 读取单条配置值（按 config_key）
     *
     * @param configKey 配置键
     * @return 配置值（不存在返回 null）
     */
    @GetMapping("/feign-api/v1/system/app-config/{configKey}")
    Result<String> getConfigValue(@PathVariable("configKey") String configKey);

    /**
     * 写入单条配置值（不存在则新建，成功后清理 APP_CONFIG 缓存）
     *
     * @param configKey 配置键
     * @param dto       配置值
     * @return 是否成功
     */
    @PutMapping("/feign-api/v1/system/app-config/{configKey}")
    Result<Boolean> saveConfigValue(@PathVariable("configKey") String configKey,
            @RequestBody SysAppConfigValueDTO dto);
}
