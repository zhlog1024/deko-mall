package com.deko.mall.system.controller.app;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.deko.mall.common.result.Result;
import com.deko.mall.system.mapper.SysAppConfigMapper;
import com.deko.mall.system.mapper.SysCountryCodeMapper;
import com.deko.mall.system.model.entity.SysAppConfig;
import com.deko.mall.system.model.entity.SysCountryCode;
import com.deko.mall.system.model.vo.CountryCodeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * APP端 - 国家区号
 */
@Slf4j
@Tag(name = "App-国家区号")
@RestController
@RequestMapping("/app-api/v1/country-codes")
@RequiredArgsConstructor
public class CountryCodeController {

    private static final String CACHE_KEY = "country_codes:enabled";
    private static final long CACHE_TTL_HOURS = 1;

    private final SysAppConfigMapper appConfigMapper;
    private final SysCountryCodeMapper countryCodeMapper;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Operation(summary = "获取可用国家区号列表",
            description = "根据 enable_international_phone 和 international_phone_countries 白名单返回可用国家。开关关闭时仅返回 +86。")
    @GetMapping
    @SuppressWarnings("unchecked")
    public Result<List<CountryCodeVO>> getEnabledCountryCodes() {
        // 从 Redis 缓存取
        Object cached = redisTemplate.opsForValue().get(CACHE_KEY);
        if (cached instanceof List<?> list && !list.isEmpty()) {
            return Result.success((List<CountryCodeVO>) list);
        }

        List<CountryCodeVO> result = buildList();
        if (!result.isEmpty()) {
            redisTemplate.opsForValue().set(CACHE_KEY, result, CACHE_TTL_HOURS, TimeUnit.HOURS);
        }
        return Result.success(result);
    }

    private List<CountryCodeVO> buildList() {
        // 1. 检查开关
        SysAppConfig enableSwitch = appConfigMapper.selectOne(
                new LambdaQueryWrapper<SysAppConfig>()
                        .eq(SysAppConfig::getConfigKey, "enable_international_phone")
                        .eq(SysAppConfig::getStatus, 1));

        boolean internationalEnabled = enableSwitch != null && "1".equals(enableSwitch.getConfigValue());

        // 2. 开关关闭，仅返回中国
        if (!internationalEnabled) {
            SysCountryCode china = countryCodeMapper.selectOne(
                    new LambdaQueryWrapper<SysCountryCode>()
                            .eq(SysCountryCode::getIsoCode, "CN")
                            .eq(SysCountryCode::getStatus, 1));
            if (china != null) {
                return Collections.singletonList(toVO(china));
            }
            return Collections.emptyList();
        }

        // 3. 读取白名单（国家英文名，如 Kenya）
        List<String> allowedNames = getAllowedCountryNames();

        // 4. 查询启用的国家
        List<SysCountryCode> countries = countryCodeMapper.selectList(
                new LambdaQueryWrapper<SysCountryCode>()
                        .eq(SysCountryCode::getStatus, 1)
                        .orderByAsc(SysCountryCode::getSort));

        // 5. 按白名单过滤（白名单为空则不限制）
        if (allowedNames != null && !allowedNames.isEmpty()) {
            countries = countries.stream()
                    .filter(c -> allowedNames.contains(c.getNameEn()))
                    .collect(Collectors.toList());
        }

        return countries.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    /**
     * 从 sys_app_config 读取白名单
     */
    private List<String> getAllowedCountryNames() {
        SysAppConfig config = appConfigMapper.selectOne(
                new LambdaQueryWrapper<SysAppConfig>()
                        .eq(SysAppConfig::getConfigKey, "international_phone_countries")
                        .eq(SysAppConfig::getStatus, 1));
        if (config == null || StrUtil.isBlank(config.getConfigValue())) {
            return null;
        }
        try {
            return objectMapper.readValue(config.getConfigValue(), new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("解析国家白名单失败: {}", config.getConfigValue(), e);
            return null;
        }
    }

    private CountryCodeVO toVO(SysCountryCode entity) {
        return CountryCodeVO.builder()
                .isoCode(entity.getIsoCode())
                .nameZh(entity.getNameZh())
                .nameEn(entity.getNameEn())
                .phoneCode(entity.getPhoneCode())
                .phoneLength(entity.getPhoneLength())
                .flagEmoji(entity.getFlagEmoji())
                .build();
    }
}
