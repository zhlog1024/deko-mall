package com.deko.mall.sms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.deko.mall.sms.model.entity.SmsLiveRoom;
import org.apache.ibatis.annotations.Mapper;

/**
 * 直播间 Mapper
 */
@Mapper
public interface LiveRoomMapper extends BaseMapper<SmsLiveRoom> {
}
