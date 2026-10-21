package com.deko.mall.sms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.sms.model.entity.SmsAdvert;
import com.deko.mall.sms.model.query.AdvertPageQuery;
import com.deko.mall.sms.model.vo.AdvertPageVO;
import com.deko.mall.sms.model.vo.BannerVO;

import java.util.List;

public interface AdvertService extends IService<SmsAdvert> {

    /**
     * 广告分页列表
     */
    Page<AdvertPageVO> getAdvertPage(AdvertPageQuery queryParams);

    List<BannerVO> getBannerList();
}
