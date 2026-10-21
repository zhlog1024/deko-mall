package com.deko.mall.system.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.deko.mall.system.converter.SysAppVersionConverter;
import com.deko.mall.system.mapper.SysAppVersionMapper;
import com.deko.mall.system.model.dto.AppVersionCheckDTO;
import com.deko.mall.system.model.dto.AppVersionCheckDTO.UpdateData;
import com.deko.mall.system.model.entity.SysAppVersion;
import com.deko.mall.system.model.form.AppVersionForm;
import com.deko.mall.system.model.query.AppVersionPageQuery;
import com.deko.mall.system.model.vo.AppVersionVO;
import com.deko.mall.system.service.SysAppVersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysAppVersionServiceImpl extends ServiceImpl<SysAppVersionMapper, SysAppVersion> implements SysAppVersionService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final SysAppVersionConverter converter;

    @Override
    public Page<AppVersionVO> getPage(AppVersionPageQuery queryParams) {
        Page<SysAppVersion> page = this.page(
                new Page<>(queryParams.getPageNum(), queryParams.getPageSize()),
                new LambdaQueryWrapper<SysAppVersion>()
                        .like(StrUtil.isNotBlank(queryParams.getKeywords()), SysAppVersion::getVersionName, queryParams.getKeywords())
                        .or()
                        .like(StrUtil.isNotBlank(queryParams.getKeywords()), SysAppVersion::getVersionCode, queryParams.getKeywords())
                        .eq(StrUtil.isNotBlank(queryParams.getPlatform()), SysAppVersion::getPlatform, queryParams.getPlatform())
                        .eq(queryParams.getIsEnabled() != null, SysAppVersion::getIsEnabled, queryParams.getIsEnabled())
                        .orderByDesc(SysAppVersion::getCreateTime)
        );
        return converter.entity2Page(page);
    }

    @Override
    public AppVersionVO getDetail(Long id) {
        SysAppVersion entity = this.getById(id);
        Assert.isTrue(entity != null, "APP版本不存在");
        return converter.entity2VO(entity);
    }

    @Override
    @Transactional
    public boolean add(AppVersionForm form) {
        if (form.getIsEnabled() != null && form.getIsEnabled() == 1) {
            disableEnabledVersions(form.getPlatform(), null);
        }
        SysAppVersion entity = converter.form2Entity(form);
        return this.save(entity);
    }

    @Override
    @Transactional
    public boolean update(Long id, AppVersionForm form) {
        SysAppVersion entity = this.getById(id);
        Assert.isTrue(entity != null, "APP版本不存在");
        if (form.getIsEnabled() != null && form.getIsEnabled() == 1) {
            disableEnabledVersions(form.getPlatform(), id);
        }
        SysAppVersion updateEntity = converter.form2Entity(form);
        updateEntity.setId(id);
        return this.updateById(updateEntity);
    }

    @Override
    @Transactional
    public boolean delete(String idsStr) {
        Assert.isTrue(StrUtil.isNotBlank(idsStr), "删除数据为空");
        List<Long> ids = Arrays.stream(idsStr.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
        return this.removeByIds(ids);
    }

    @Override
    public AppVersionCheckDTO checkUpdate(String appVersion, String platform) {
        SysAppVersion latestVersion = this.getOne(new LambdaQueryWrapper<SysAppVersion>()
                .eq(SysAppVersion::getPlatform, platform)
                .eq(SysAppVersion::getIsEnabled, 1)
                .orderByDesc(SysAppVersion::getId)
                .last("LIMIT 1")
        );

        if (latestVersion == null) {
            return noUpdate();
        }

        String clientVer = normalizeVersion(appVersion);
        String serverVer = normalizeVersion(latestVersion.getVersionCode());

        if (compareVersion(serverVer, clientVer) <= 0) {
            return noUpdate();
        }

        AppVersionCheckDTO dto = new AppVersionCheckDTO();
        dto.setIsUpdateStatus(1);
        dto.setUpdateTipsIntervalTime(600);

        UpdateData updateData = new UpdateData();
        updateData.setName(latestVersion.getVersionName());
        updateData.setVersionNew(latestVersion.getVersionCode());
        updateData.setUpdateUrl(latestVersion.getUpdateUrl());
        updateData.setIsForceUpdate(latestVersion.getIsForceUpdate());

        try {
            if (StrUtil.isNotBlank(latestVersion.getUpdateContent())) {
                List<String> content = OBJECT_MAPPER.readValue(
                        latestVersion.getUpdateContent(),
                        new TypeReference<List<String>>() {}
                );
                updateData.setContent(content);
            }
        } catch (Exception e) {
            log.warn("解析更新内容JSON失败: {}", e.getMessage());
            updateData.setContent(Collections.emptyList());
        }

        dto.setUpdateData(updateData);
        return dto;
    }

    private void disableEnabledVersions(String platform, Long excludeId) {
        this.update(new LambdaUpdateWrapper<SysAppVersion>()
                .eq(SysAppVersion::getPlatform, platform)
                .eq(SysAppVersion::getIsEnabled, 1)
                .ne(excludeId != null, SysAppVersion::getId, excludeId)
                .set(SysAppVersion::getIsEnabled, 0)
        );
    }

    private AppVersionCheckDTO noUpdate() {
        AppVersionCheckDTO dto = new AppVersionCheckDTO();
        dto.setIsUpdateStatus(0);
        return dto;
    }

    private String normalizeVersion(String rawVersion) {
        if (StrUtil.isBlank(rawVersion)) {
            return "0";
        }
        String ver = rawVersion.trim();
        if (ver.startsWith("v") || ver.startsWith("V")) {
            ver = ver.substring(1);
        }
        int spaceIdx = ver.indexOf(' ');
        if (spaceIdx > 0) {
            ver = ver.substring(0, spaceIdx);
        }
        return ver;
    }

    private int compareVersion(String v1, String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");
        int maxLen = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < maxLen; i++) {
            int n1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int n2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
            if (n1 != n2) {
                return n1 - n2;
            }
        }
        return 0;
    }
}
