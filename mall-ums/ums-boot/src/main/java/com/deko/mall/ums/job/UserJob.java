package com.deko.mall.ums.job;

import cn.hutool.core.collection.CollectionUtil;
import com.deko.mall.ums.service.UmsMemberAssetsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 会员相关定时任务。
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class UserJob {

    private final UmsMemberAssetsService umsMemberAssetsService;

    /**
     * 资产初始化
     */
    @Scheduled(cron = "30 * * * * *")
    public void intAsset() {
        log.info("--------资产初始化job------");
        List<Long> userIds = umsMemberAssetsService.getNeedInitUser();
        if (CollectionUtil.isEmpty(userIds)) {
            return;
        }
        for (Long userId : userIds) {
            umsMemberAssetsService.initAllMemberAssets(userId);
        }
    }
}
