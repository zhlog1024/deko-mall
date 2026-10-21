package com.deko.mall.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.deko.mall.oms.model.entity.logistics.OmsLogisticsTrack;
import org.apache.ibatis.annotations.Mapper;

/**
 * 物流轨迹Mapper接口
 */
@Mapper
public interface LogisticsTrackMapper extends BaseMapper<OmsLogisticsTrack> {
}
