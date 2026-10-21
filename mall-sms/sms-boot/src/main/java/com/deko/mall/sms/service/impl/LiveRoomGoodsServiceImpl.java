package com.deko.mall.sms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.sms.mapper.LiveRoomGoodsMapper;
import com.deko.mall.sms.model.entity.SmsLiveRoomGoods;
import com.deko.mall.sms.service.LiveRoomGoodsService;
import org.springframework.stereotype.Service;

/**
 * 直播商品挂载服务实现
 */
@Service
public class LiveRoomGoodsServiceImpl extends ServiceImpl<LiveRoomGoodsMapper, SmsLiveRoomGoods>
        implements LiveRoomGoodsService {
}
