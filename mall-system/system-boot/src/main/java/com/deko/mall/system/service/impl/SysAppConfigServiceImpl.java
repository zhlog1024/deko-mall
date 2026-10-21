package com.deko.mall.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.deko.mall.system.constant.CacheKeyEnum;
import com.deko.mall.system.mapper.SysAppConfigMapper;
import com.deko.mall.system.model.entity.SysAppConfig;
import com.deko.mall.system.model.vo.AppConfigGroupVO;
import com.deko.mall.system.model.vo.AppConfigItemVO;
import com.deko.mall.system.service.SysAppConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysAppConfigServiceImpl implements SysAppConfigService {

    private final SysAppConfigMapper mapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String APP_CONFIG_CACHE_KEY = CacheKeyEnum.APP_CONFIG.getKey();
    private static final Long CACHE_EXPIRE_SECONDS = 24 * 60 * 60L;

    /** 分组代码 -> 分组名称 映射 */
    private static final Map<String, String> GROUP_NAME_MAP = new LinkedHashMap<>();

    /** 运营后台与 App 配置均不再暴露的键（UI 静态仅包内） */
    private static final Set<String> HIDDEN_CONFIG_KEYS = Set.of("common_static_url");

    static {
        GROUP_NAME_MAP.put("base", "基础信息");
        GROUP_NAME_MAP.put("theme", "主题设置");
        GROUP_NAME_MAP.put("function", "功能开关");
        GROUP_NAME_MAP.put("customer_service", "在线客服");
        GROUP_NAME_MAP.put("category", "分类页设置");
        GROUP_NAME_MAP.put("user", "用户相关");
        GROUP_NAME_MAP.put("share", "分享与搜索");
    }

    @Override
    public List<AppConfigGroupVO> getGroupedConfigs() {
        List<SysAppConfig> allConfigs = mapper.selectList(
                new LambdaQueryWrapper<SysAppConfig>()
                        .eq(SysAppConfig::getStatus, 1)
                        .orderByAsc(SysAppConfig::getSort));

        return GROUP_NAME_MAP.entrySet().stream()
                .map(entry -> {
                    String groupCode = entry.getKey();
                    List<SysAppConfig> groupItems = allConfigs.stream()
                            .filter(c -> groupCode.equals(c.getGroupCode()))
                            .filter(c -> !HIDDEN_CONFIG_KEYS.contains(c.getConfigKey()))
                            .collect(Collectors.toList());

                    if (groupItems.isEmpty()) {
                        return null;
                    }

                    AppConfigGroupVO groupVO = new AppConfigGroupVO();
                    groupVO.setGroupCode(groupCode);
                    groupVO.setGroupName(entry.getValue());
                    groupVO.setItems(groupItems.stream().map(this::toItemVO).collect(Collectors.toList()));
                    return groupVO;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public boolean saveConfigs(Map<String, Object> configMap) {
        List<SysAppConfig> existingConfigs = mapper.selectList(
                new LambdaQueryWrapper<SysAppConfig>()
                        .eq(SysAppConfig::getStatus, 1));

        Map<String, SysAppConfig> existingMap = existingConfigs.stream()
                .collect(Collectors.toMap(SysAppConfig::getConfigKey, c -> c, (a, b) -> a));

        for (Map.Entry<String, Object> entry : configMap.entrySet()) {
            String key = entry.getKey();
            if (HIDDEN_CONFIG_KEYS.contains(key)) {
                continue;
            }
            Object value = entry.getValue();
            String strValue = value instanceof String ? (String) value : String.valueOf(value);

            SysAppConfig existing = existingMap.get(key);
            if (existing != null) {
                existing.setConfigValue(strValue);
                mapper.updateById(existing);
            }
        }

        clearCache();
        log.info("App config updated, {} keys changed", configMap.size());
        return true;
    }

    @Override
    public String getConfigValue(String configKey) {
        SysAppConfig config = mapper.selectOne(
                new LambdaQueryWrapper<SysAppConfig>()
                        .eq(SysAppConfig::getConfigKey, configKey)
                        .last("LIMIT 1"));
        return config == null ? null : config.getConfigValue();
    }

    @Override
    public boolean saveConfigValue(String configKey, String configValue) {
        if (HIDDEN_CONFIG_KEYS.contains(configKey)) {
            log.info("Ignore save for hidden config key[{}]", configKey);
            return true;
        }
        SysAppConfig existing = mapper.selectOne(
                new LambdaQueryWrapper<SysAppConfig>()
                        .eq(SysAppConfig::getConfigKey, configKey)
                        .last("LIMIT 1"));
        if (existing == null) {
            SysAppConfig config = new SysAppConfig();
            config.setConfigKey(configKey);
            config.setConfigValue(configValue);
            config.setConfigName(configKey);
            config.setValueType("json");
            // DIY 装修数据已归位 cms（cms_diy_app_tabbar/cms_diy_home_mode），sys_app_config 不再有 tabbar 分组，
            // 新建配置行统一归入 base 基础分组
            config.setGroupCode("base");
            config.setStatus(1);
            mapper.insert(config);
        } else {
            existing.setConfigValue(configValue);
            mapper.updateById(existing);
        }
        clearCache();
        log.info("App config key[{}] saved, value length {}", configKey,
                configValue == null ? 0 : configValue.length());
        return true;
    }

    @Override
    public void clearCache() {
        redisTemplate.delete(APP_CONFIG_CACHE_KEY);
    }

    private AppConfigItemVO toItemVO(SysAppConfig entity) {
        AppConfigItemVO vo = new AppConfigItemVO();
        vo.setConfigKey(entity.getConfigKey());
        vo.setConfigName(entity.getConfigName());
        vo.setConfigValue(entity.getConfigValue());
        vo.setValueType(entity.getValueType());
        vo.setRemark(entity.getRemark());
        vo.setSort(entity.getSort());
        vo.setStatus(entity.getStatus());
        return vo;
    }
}
