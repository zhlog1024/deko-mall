package com.deko.mall.sms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deko.mall.sms.model.entity.SmsAdvert;
import com.deko.mall.sms.model.query.AdvertPageQuery;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdvertMapper extends BaseMapper<SmsAdvert> {

    /**
     * 广告分页列表
     */
    Page<SmsAdvert> getAdvertPage(Page<SmsAdvert> page, AdvertPageQuery queryParams);
}
