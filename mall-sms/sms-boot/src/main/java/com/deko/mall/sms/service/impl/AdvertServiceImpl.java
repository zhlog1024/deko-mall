package com.deko.mall.sms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.common.enums.StatusEnum;
import com.deko.mall.sms.converter.AdvertConverter;
import com.deko.mall.sms.mapper.AdvertMapper;
import com.deko.mall.sms.model.entity.SmsAdvert;
import com.deko.mall.sms.model.query.AdvertPageQuery;
import com.deko.mall.sms.model.vo.AdvertPageVO;
import com.deko.mall.sms.model.vo.BannerVO;
import com.deko.mall.sms.service.AdvertService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 广告业务实现类
 */
@Service
@RequiredArgsConstructor
public class AdvertServiceImpl extends ServiceImpl<AdvertMapper, SmsAdvert> implements AdvertService {

    private final AdvertConverter advertConverter;

    /**
     * 广告分页列表
     *
     * @param queryParams 查询参数
     * @return 广告分页列表
     */
    @Override
    public Page<AdvertPageVO> getAdvertPage(AdvertPageQuery queryParams) {
        Page<SmsAdvert> page = this.baseMapper.getAdvertPage(new Page<>(queryParams.getPageNum(),
                        queryParams.getPageSize()),
                queryParams);
        return advertConverter.entity2PageVo(page);
    }

    /**
     * 获取广告横幅列表
     */
    @Override
    public List<BannerVO> getBannerList() {

        List<SmsAdvert> entities = this.list(new LambdaQueryWrapper<SmsAdvert>().
                eq(SmsAdvert::getStatus, StatusEnum.ENABLE.getValue())
                .select(SmsAdvert::getTitle, SmsAdvert::getPicUrl, SmsAdvert::getRedirectUrl)
        );
        return advertConverter.entity2BannerVo(entities);
    }
}
