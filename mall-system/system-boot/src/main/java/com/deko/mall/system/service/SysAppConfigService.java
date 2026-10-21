package com.deko.mall.system.service;

import com.deko.mall.system.model.vo.AppConfigGroupVO;
import java.util.List;
import java.util.Map;

/**
 * APP配置管理服务（管理后台CRUD）
 */
public interface SysAppConfigService {

    /**
     * 获取所有配置（按分组返回）
     */
    List<AppConfigGroupVO> getGroupedConfigs();

    /**
     * 批量保存配置
     */
    boolean saveConfigs(Map<String, Object> configMap);

    /**
     * 按配置键读取配置值
     *
     * @param configKey 配置键
     * @return 配置值（不存在返回 null）
     */
    String getConfigValue(String configKey);

    /**
     * 按配置键保存配置值（不存在则新建，成功后清理 APP_CONFIG 缓存）
     *
     * @param configKey   配置键
     * @param configValue 配置值
     * @return 是否成功
     */
    boolean saveConfigValue(String configKey, String configValue);

    /**
     * 清除缓存
     */
    void clearCache();
}
