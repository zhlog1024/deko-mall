package com.deko.mall.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.deko.mall.common.result.Result;
import com.deko.mall.common.result.ResultCode;
import com.deko.mall.common.web.exception.BizException;
import com.deko.mall.cms.converter.DiyPageConverter;
import com.deko.mall.cms.mapper.DiyPageMapper;
import com.deko.mall.cms.model.dto.DiyPageHomeDTO;
import com.deko.mall.cms.model.dto.DiyPagePublishDTO;
import com.deko.mall.cms.model.dto.DiyPageSaveDTO;
import com.deko.mall.cms.model.entity.DiyHomeMode;
import com.deko.mall.cms.model.entity.DiyPage;
import com.deko.mall.cms.model.query.DiyPageQuery;
import com.deko.mall.cms.model.vo.DiyPageAppVO;
import com.deko.mall.cms.model.vo.DiyPageVO;
import com.deko.mall.cms.service.DiyHomeModeService;
import com.deko.mall.cms.service.DiyPageService;
import com.deko.mall.cms.service.assemble.DiyAssembleService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DiyPageServiceImpl extends ServiceImpl<DiyPageMapper, DiyPage> implements DiyPageService {

    private final DiyPageConverter diyPageConverter;
    private final DiyAssembleService diyAssembleService;
    private final DiyHomeModeService diyHomeModeService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 支持的客户端类型 */
    private static final List<String> SUPPORTED_CLIENT_TYPES = List.of("weixin", "app", "h5");

    /** 默认客户端类型：请求未携带端标识时的兜底（当前主端为微信小程序） */
    private static final String DEFAULT_CLIENT_TYPE = "weixin";
    
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * DIY页面缓存Key前缀
     */
    private static final String DIY_PAGE_CACHE_KEY = "diy:page:";
    
    /**
     * 缓存过期时间（秒）- 24小时
     */
    private static final long CACHE_EXPIRE_SECONDS = 24 * 60 * 60;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveOrUpdatePage(DiyPageSaveDTO dto) {
        DiyPage entity = diyPageConverter.toEntity(dto);
        LocalDateTime now = LocalDateTime.now();
        boolean isCreate = dto.getId() == null;
        if (entity.getIsEnabled() == null) {
            entity.setIsEnabled(Boolean.FALSE);
        }
        if (isCreate) {
            entity.setIsHome(Boolean.FALSE);
            entity.setMd5Key(genMd5());
        }
        entity.setUpdateTime(now);
        if (isCreate) {
            entity.setCreateTime(now);
            save(entity);
        } else {
            updateById(entity);
            // 更新操作清除缓存
            clearPageCache(entity.getId());
        }
    
        // 启用状态直接来自 isEnabled，不再区分"预发布时间段"
        if (Boolean.TRUE.equals(dto.getIsEnabled())) {
            DiyPagePublishDTO publishDTO = new DiyPagePublishDTO();
            publishDTO.setId(entity.getId());
            publishDTO.setIsEnabled(true);
            publish(publishDTO);
        }
            
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(DiyPagePublishDTO dto) {
        DiyPage page = getById(dto.getId());
        Assert.notNull(page, "装修页面不存在");
        page.setIsEnabled(dto.getIsEnabled() == null || dto.getIsEnabled());
        page.setUpdateTime(LocalDateTime.now());
        updateById(page);
        
        // 清除缓存
        clearPageCache(page.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setHome(DiyPageHomeDTO dto) {
        DiyPage page = getById(dto.getId());
        Assert.notNull(page, "装修页面不存在");
        
        // 1. 查询旧首页
        LambdaQueryWrapper<DiyPage> wrapper = new LambdaQueryWrapper<DiyPage>()
                .eq(DiyPage::getIsEnabled, true)
                .eq(DiyPage::getIsHome, true)
                .last("limit 1");
        DiyPage oldHome = baseMapper.selectOne(wrapper);
        
        LambdaUpdateWrapper<DiyPage> clearWrapper = new LambdaUpdateWrapper<DiyPage>()
                .set(DiyPage::getIsHome, false);
        update(clearWrapper);

        // 3. 设置新首页
        DiyPage update = new DiyPage();
        update.setId(page.getId());
        update.setIsHome(true);
        update.setUpdateTime(LocalDateTime.now());
        updateById(update);
        // 4. 清除缓存
        clearPageCache(page.getId());
    }

    @Override
    public IPage<DiyPageVO> page(DiyPageQuery query) {
        Page<DiyPage> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<DiyPage> wrapper = new LambdaQueryWrapper<DiyPage>()
                .eq(query.getIsEnabled() != null, DiyPage::getIsEnabled, query.getIsEnabled())
                .like(StringUtils.hasText(query.getKeyword()), DiyPage::getName, query.getKeyword())
                .orderByDesc(DiyPage::getUpdateTime);
        IPage<DiyPage> entityPage = baseMapper.selectPage(page, wrapper);
        List<DiyPageVO> voList = entityPage.getRecords().stream()
                .map(diyPageConverter::toVO)
                .collect(Collectors.toList());
        Page<DiyPageVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public DiyPageVO getDetail(Long id) {
        DiyPage page = getById(id);
        return page == null ? null : diyPageConverter.toVO(page);
    }

    @Override
    public DiyPageVO getHome() {
        // 按是否启用 + 是否首页筛选，取最新一条
        LambdaQueryWrapper<DiyPage> wrapper = new LambdaQueryWrapper<DiyPage>()
                .eq(DiyPage::getIsEnabled, true)
                .eq(DiyPage::getIsHome, true)
                .orderByDesc(DiyPage::getUpdateTime)
                .last("limit 1");
        DiyPage page = baseMapper.selectOne(wrapper);
        return page == null ? null : diyPageConverter.toVO(page);
    }

    @Override
    public DiyPageAppVO getAppHome() {
        // 兼容无端标识调用（历史/内部），默认按主端 weixin
        return getAppHome(null);
    }

    @Override
    public DiyPageAppVO getAppHome(String clientType) {
        // 1. 解析端类型（缺省/不支持 → 默认 weixin，记日志）
        String resolvedType = resolveClientType(clientType);

        // 2. 按端读取首页配置（diy_id + 定时）
        //    ① 配置锁定（按端取 diy_id）② 定时锁定（time_start/time_end）③ 启用锁定（is_enabled=true）
        DiyPage page = resolveHomeDiyPage(resolvedType);

        if (page == null) {
            // DIY 首页未命中返回 null（前端回退普通首页/空数据）
            log.debug("首页 DIY 未命中（配置缺失/定时外/未启用）, clientType: {}", resolvedType);
            return null;
        }

        // 3. 服务端装配 auto 数据，故不做整 VO 缓存
        log.debug("首页缓存未命中或已旁路，从数据库加载并装配, pageId: {}, clientType: {}", page.getId(), resolvedType);
        return convertToAppVO(page);
    }

    /**
     * 解析客户端类型：支持 weixin/app/h5；null/空/不支持值 → 默认 weixin
     */
    private String resolveClientType(String clientType) {
        if (clientType != null && SUPPORTED_CLIENT_TYPES.contains(clientType)) {
            return clientType;
        }
        if (clientType != null && !clientType.isEmpty()) {
            log.warn("首页 DIY 请求携带未支持的 clientType[{}]，按默认端[{}]处理", clientType, DEFAULT_CLIENT_TYPE);
        }
        return DEFAULT_CLIENT_TYPE;
    }

    /**
     * 按端解析首页 DIY 页面（配置锁定 + 定时锁定 + 启用锁定）
     * 数据源 cms_diy_home_mode 本地表
     */
    private DiyPage resolveHomeDiyPage(String clientType) {
        // ① 配置锁定 + ② 定时锁定：按端查 cms_diy_home_mode 行（定时窗口内）
        DiyHomeMode mode = diyHomeModeService.getByClientTypeInWindow(clientType);
        if (mode == null) {
            // 当前端未配置/定时外 → 无首页（精确匹配，不跨端回退）
            log.debug("首页 DIY 配置未命中(未配置或定时外), clientType: {}", clientType);
            return null;
        }
        // ③ 启用锁定：页面必须 is_enabled=true
        DiyPage page = getById(mode.getDiyPageId());
        if (page == null || !Boolean.TRUE.equals(page.getIsEnabled())) {
            log.debug("首页 DIY 页面不存在或未启用, clientType: {}, diyPageId: {}", clientType, mode.getDiyPageId());
            return null;
        }
        return page;
    }

    @Override
    public Map<String, Object> getHomeModeConfig() {
        return diyHomeModeService.getHomeModeConfigMap();
    }

    @Override
    public void saveHomeModeConfig(Map<String, Object> config) {
        diyHomeModeService.saveHomeModeConfigMap(config);
    }

    @Override
    public DiyPageAppVO getAppDetail(Long id) {
        if (id == null) {
            return null;
        }

        // 服务端装配 auto 数据（每次请求实时），故旁路整 VO 缓存
        log.debug("页面缓存旁路，从数据库加载并装配, pageId: {}", id);
        DiyPage page = getById(id);
        // DiyData：App 端读取仅返回已启用(is_enabled=true)页面，停用页视为不存在
        if (page == null || !Boolean.TRUE.equals(page.getIsEnabled())) {
            return null;
        }
        return convertToAppVO(page);
    }

    /**
     * 转换为 App 端 VO（解析 JSON 配置）
     */
    private DiyPageAppVO convertToAppVO(DiyPage page) {
        if (page == null) {
            return null;
        }

        DiyPageAppVO appVO = new DiyPageAppVO();
        DiyPageAppVO.DiyPageDataVO dataVO = new DiyPageAppVO.DiyPageDataVO();

        dataVO.setId(page.getId());
        dataVO.setName(page.getName());
        dataVO.setDescribe(""); // 可以根据需要添加 describe 字段
        dataVO.setLogo(page.getCoverUrl()); // 使用 coverUrl 作为 logo
        dataVO.setCoverUrl(page.getCoverUrl());
        dataVO.setIsHome(page.getIsHome());
        dataVO.setIsEnabled(page.getIsEnabled());

        // 解析 JSON 配置为对象，并对 auto 组件做服务端实时装配
        try {
            if (StringUtils.hasText(page.getConfig())) {
                ObjectNode config = (ObjectNode) objectMapper.readTree(page.getConfig());
                // 服务端装配：遍历组件按 key 拉取 auto 最新数据（article/ask/plugins-video 等）
                diyAssembleService.assemble(config);
                dataVO.setConfig(config);
            }
        } catch (Exception e) {
            log.error("解析/装配 DIY 页面配置失败, pageId: {}, 回退原始 config", page.getId(), e);
            try {
                if (StringUtils.hasText(page.getConfig())) {
                    Object config = objectMapper.readValue(page.getConfig(), Object.class);
                    dataVO.setConfig(config);
                }
            } catch (Exception ex) {
                log.error("回退解析 DIY 页面配置失败, pageId: {}", page.getId(), ex);
                dataVO.setConfig(null);
            }
        }

        appVO.setData(dataVO);
        appVO.setIsResultDataCache(0);

        return appVO;
    }

    @Override
    public void disable(Long id) {
        DiyPage page = new DiyPage();
        page.setId(id);
        page.setIsEnabled(false);
        page.setUpdateTime(LocalDateTime.now());
        updateById(page);
        
        // 清除缓存
        clearPageCache(id);
    }

    @Override
    public void setEnabled(Long id, Boolean isEnabled) {
        log.info("设置装修页面启用状态, id: {}, isEnabled: {}", id, isEnabled);
        Assert.notNull(getById(id), "装修页面不存在");
        DiyPage page = new DiyPage();
        page.setId(id);
        page.setIsEnabled(isEnabled);
        page.setUpdateTime(LocalDateTime.now());
        updateById(page);
        
        // 清除缓存
        clearPageCache(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        log.info("删除装修页面, id: {}", id);
        DiyPage page = getById(id);
        Assert.notNull(page, "装修页面不存在");
        
        removeById(id);
        
        // 清除缓存
        clearPageCache(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long copy(Long id) {
        log.info("复制装修页面, id: {}", id);
        DiyPage original = getById(id);
        Assert.notNull(original, "装修页面不存在");

        LocalDateTime now = LocalDateTime.now();
        DiyPage copyPage = new DiyPage();
        copyPage.setName(original.getName() + " 副本");
        copyPage.setConfig(original.getConfig());
        copyPage.setCoverUrl(original.getCoverUrl());
        copyPage.setIsHome(false);
        copyPage.setIsEnabled(false);
        copyPage.setMd5Key(genMd5());
        copyPage.setCreateTime(now);
        copyPage.setUpdateTime(now);
        save(copyPage);

        log.info("复制装修页面完成, originalId: {}, newId: {}", id, copyPage.getId());
        return copyPage.getId();
    }

    /**
     * 获取页面缓存Key
     */
    private String getPageCacheKey(Long pageId) {
        return DIY_PAGE_CACHE_KEY + pageId;
    }
    
    /**
     * 清除指定页面的缓存
     */
    private void clearPageCache(Long pageId) {
        if (pageId == null) {
            return;
        }
        String cacheKey = getPageCacheKey(pageId);
        redisTemplate.delete(cacheKey);
        log.debug("清除页面缓存, cacheKey: {}", cacheKey);
    }

    private String genMd5() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

