package com.deko.mall.ums.controller.feign;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.common.result.Result;
import com.deko.mall.ums.api.GoodsBrowseFeignClient;
import com.deko.mall.ums.mapper.UmsGoodsBrowseMapper;
import com.deko.mall.ums.model.entity.UmsGoodsBrowse;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GoodsBrowseFeignController extends ServiceImpl<UmsGoodsBrowseMapper, UmsGoodsBrowse> implements GoodsBrowseFeignClient {

    @Override
    public Result<Void> recordBrowse(Long memberId, Long spuId) {
        // 去重
        this.update(new LambdaUpdateWrapper<UmsGoodsBrowse>()
                .eq(UmsGoodsBrowse::getMemberId, memberId)
                .eq(UmsGoodsBrowse::getSpuId, spuId)
                .set(UmsGoodsBrowse::getDeleted, 1));
        UmsGoodsBrowse browse = new UmsGoodsBrowse();
        browse.setMemberId(memberId);
        browse.setSpuId(spuId);
        this.save(browse);
        return Result.success();
    }
}
