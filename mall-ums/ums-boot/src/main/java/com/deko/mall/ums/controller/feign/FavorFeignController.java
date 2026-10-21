package com.deko.mall.ums.controller.feign;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.common.result.Result;
import com.deko.mall.ums.api.FavorFeignClient;
import com.deko.mall.ums.mapper.UmsFavorMapper;
import com.deko.mall.ums.model.entity.UmsFavor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

/**
 * 会员统一收藏 Feign 实现
 */
@RestController
@RequiredArgsConstructor
public class FavorFeignController extends ServiceImpl<UmsFavorMapper, UmsFavor> implements FavorFeignClient {

    @Override
    public Result<Boolean> isFavored(Long memberId, String targetType, Long targetId) {
        long count = this.count(new LambdaQueryWrapper<UmsFavor>()
                .eq(UmsFavor::getMemberId, memberId)
                .eq(UmsFavor::getTargetType, targetType)
                .eq(UmsFavor::getTargetId, targetId)
                .eq(UmsFavor::getDeleted, 0));
        return Result.success(count > 0);
    }

    @Override
    public Result<Long> countByTarget(String targetType, Long targetId) {
        long count = this.count(new LambdaQueryWrapper<UmsFavor>()
                .eq(UmsFavor::getTargetType, targetType)
                .eq(UmsFavor::getTargetId, targetId)
                .eq(UmsFavor::getDeleted, 0));
        return Result.success(count);
    }
}
