package com.deko.mall.sms.converter;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deko.mall.sms.model.entity.SmsAdvert;
import com.deko.mall.sms.model.vo.AdvertPageVO;
import com.deko.mall.sms.model.vo.BannerVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * advert实体转换器
 */
@Mapper(componentModel = "spring")
public interface AdvertConverter {

    AdvertPageVO entity2PageVo(SmsAdvert entity);

    Page<AdvertPageVO> entity2PageVo(Page<SmsAdvert> po);

    /**
     * 广告位 → 横幅
     * <p>
     * 实体用 picUrl、VO 用 imageUrl，字段名不同必须显式映射；
     * 漏写会静默为 null（曾导致 app 首页横幅与直播首页轮播图全部无图）。
     * title / redirectUrl 同名，MapStruct 自动映射。
     */
    @Mapping(target = "imageUrl", source = "picUrl")
    BannerVO entity2BannerVo(SmsAdvert entity);

    List<BannerVO> entity2BannerVo(List<SmsAdvert> entities);
}