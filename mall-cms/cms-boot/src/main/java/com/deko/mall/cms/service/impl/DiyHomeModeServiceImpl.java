package com.deko.mall.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.common.result.ResultCode;
import com.deko.mall.common.web.exception.BizException;
import com.deko.mall.cms.mapper.DiyHomeModeMapper;
import com.deko.mall.cms.model.entity.DiyHomeMode;
import com.deko.mall.cms.service.DiyHomeModeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DIY 首页按端配置服务实现
 * 本地表 cms_diy_home_mode 每端一行，覆盖保存时按 client_type upsert
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiyHomeModeServiceImpl extends ServiceImpl<DiyHomeModeMapper, DiyHomeMode>
        implements DiyHomeModeService {

    /** 支持的客户端类型 */
    private static final List<String> SUPPORTED_CLIENT_TYPES = List.of("weixin", "app", "h5");

    @Override
    public Map<String, Object> getHomeModeConfigMap() {
        List<DiyHomeMode> all = list();
        Map<String, Object> result = new LinkedHashMap<>();
        for (DiyHomeMode mode : all) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("diyId", mode.getDiyPageId());
            item.put("timeStart", mode.getTimeStart() == null ? null : mode.getTimeStart().toString());
            item.put("timeEnd", mode.getTimeEnd() == null ? null : mode.getTimeEnd().toString());
            result.put(mode.getClientType(), item);
        }
        return result;
    }

    @Override
    public void saveHomeModeConfigMap(Map<String, Object> configMap) {
        if (configMap == null || configMap.isEmpty()) {
            throw new BizException(ResultCode.PARAM_ERROR, "首页 DIY 配置不能为空");
        }
        boolean hasValid = false;
        // 逐端解析（前端契约：key=client_type, value={diy_id,time_start,time_end}）
        for (String clientType : SUPPORTED_CLIENT_TYPES) {
            Object raw = configMap.get(clientType);
            if (!(raw instanceof Map<?, ?> mode)) {
                continue;
            }
            Object diyIdObj = mode.get("diy_id") != null ? mode.get("diy_id") : mode.get("diyId");
            if (diyIdObj == null || String.valueOf(diyIdObj).isBlank()) {
                continue;
            }
            long diyPageId;
            try {
                diyPageId = Long.parseLong(String.valueOf(diyIdObj));
            } catch (NumberFormatException e) {
                throw new BizException(ResultCode.PARAM_ERROR, "首页 DIY 配置 diy_id 非法: " + diyIdObj);
            }
            LocalDateTime timeStart = parseTime(mode.get("time_start") != null ? mode.get("time_start") : mode.get("timeStart"));
            LocalDateTime timeEnd = parseTime(mode.get("time_end") != null ? mode.get("time_end") : mode.get("timeEnd"));
            upsert(clientType, diyPageId, timeStart, timeEnd);
            hasValid = true;
        }
        if (!hasValid) {
            throw new BizException(ResultCode.PARAM_ERROR, "首页 DIY 配置需至少指定一个有效端的 diy_id");
        }
    }

    @Override
    public DiyHomeMode getByClientTypeInWindow(String clientType) {
        DiyHomeMode mode = getOne(new LambdaQueryWrapper<DiyHomeMode>()
                .eq(DiyHomeMode::getClientType, clientType)
                .last("LIMIT 1"));
        if (mode == null) {
            return null;
        }
        // 定时窗口校验：time_start <= now <= time_end（仅当配置时）
        LocalDateTime now = LocalDateTime.now();
        if (mode.getTimeStart() != null && now.isBefore(mode.getTimeStart())) {
            return null;
        }
        if (mode.getTimeEnd() != null && now.isAfter(mode.getTimeEnd())) {
            return null;
        }
        return mode;
    }

    /**
     * 按端 upsert 单行
     */
    private void upsert(String clientType, Long diyPageId, LocalDateTime timeStart, LocalDateTime timeEnd) {
        DiyHomeMode existing = getOne(new LambdaQueryWrapper<DiyHomeMode>()
                .eq(DiyHomeMode::getClientType, clientType)
                .last("LIMIT 1"));
        LocalDateTime now = LocalDateTime.now();
        if (existing == null) {
            DiyHomeMode mode = new DiyHomeMode();
            mode.setClientType(clientType);
            mode.setDiyPageId(diyPageId);
            mode.setTimeStart(timeStart);
            mode.setTimeEnd(timeEnd);
            mode.setCreateTime(now);
            mode.setUpdateTime(now);
            mode.setDeleted(0);
            save(mode);
        } else {
            existing.setDiyPageId(diyPageId);
            existing.setTimeStart(timeStart);
            existing.setTimeEnd(timeEnd);
            existing.setUpdateTime(now);
            updateById(existing);
        }
    }

    /**
     * 解析时间（yyyy-MM-dd HH:mm:ss 或 ISO；空返回 null）
     */
    private LocalDateTime parseTime(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return null;
        }
        String text = String.valueOf(value).trim();
        try {
            return LocalDateTime.parse(text);
        } catch (Exception ignored) {
            java.time.format.DateTimeFormatter fmt =
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            try {
                return LocalDateTime.parse(text, fmt);
            } catch (Exception e2) {
                log.warn("首页 DIY 配置时间格式无法解析[{}]", text);
                return null;
            }
        }
    }
}
