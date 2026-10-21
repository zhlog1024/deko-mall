package com.deko.mall.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.deko.mall.common.result.ResultCode;
import com.deko.mall.common.web.exception.BizException;
import com.deko.mall.cms.mapper.DiyAppTabbarMapper;
import com.deko.mall.cms.model.entity.DiyAppTabbar;
import com.deko.mall.cms.service.DiyAppTabbarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * DIY 底部导航装修数据服务实现
 * 本地表 cms_diy_app_tabbar 按 business(home) 单行存取
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiyAppTabbarServiceImpl extends ServiceImpl<DiyAppTabbarMapper, DiyAppTabbar>
        implements DiyAppTabbarService {

    /** 底部导航业务标识（当前仅 home） */
    private static final String BUSINESS_HOME = "home";

    private final ObjectMapper objectMapper;

    @Override
    public Object getTabbarConfig(String business) {
        String resolved = resolveBusiness(business);
        DiyAppTabbar tabbar = getOne(new LambdaQueryWrapper<DiyAppTabbar>()
                .eq(DiyAppTabbar::getBusiness, resolved)
                .last("LIMIT 1"));
        if (tabbar == null || !StringUtils.hasText(tabbar.getConfig())) {
            return null;
        }
        try {
            // 原样返回配置对象（content/style 结构），前端按装修器契约消费
            return objectMapper.readValue(tabbar.getConfig(), Object.class);
        } catch (Exception e) {
            log.error("底部导航装修配置 JSON 解析异常, business={}", resolved, e);
            return null;
        }
    }

    @Override
    public void saveTabbarConfig(String business, Object config) {
        String resolved = resolveBusiness(business);
        if (config == null) {
            throw new BizException(ResultCode.PARAM_ERROR, "底部导航装修配置不能为空");
        }
        try {
            String configJson = objectMapper.writeValueAsString(config);
            DiyAppTabbar existing = getOne(new LambdaQueryWrapper<DiyAppTabbar>()
                    .eq(DiyAppTabbar::getBusiness, resolved)
                    .last("LIMIT 1"));
            LocalDateTime now = LocalDateTime.now();
            if (existing == null) {
                DiyAppTabbar tabbar = new DiyAppTabbar();
                tabbar.setBusiness(resolved);
                tabbar.setConfig(configJson);
                tabbar.setIsEnable(1);
                tabbar.setCreateTime(now);
                tabbar.setUpdateTime(now);
                tabbar.setDeleted(0);
                save(tabbar);
            } else {
                existing.setConfig(configJson);
                existing.setUpdateTime(now);
                updateById(existing);
            }
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("底部导航装修配置保存异常, business={}", resolved, e);
            throw new BizException(ResultCode.SYSTEM_EXECUTION_ERROR, "底部导航装修配置保存异常", e);
        }
    }

    /**
     * 业务标识归一：缺省/空视为 home
     */
    private String resolveBusiness(String business) {
        return StringUtils.hasText(business) ? business : BUSINESS_HOME;
    }
}
