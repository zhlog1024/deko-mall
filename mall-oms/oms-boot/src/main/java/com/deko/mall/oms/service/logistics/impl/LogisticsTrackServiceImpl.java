package com.deko.mall.oms.service.logistics.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.oms.mapper.LogisticsTrackMapper;
import com.deko.mall.oms.model.entity.logistics.OmsLogisticsTrack;
import com.deko.mall.oms.service.logistics.LogisticsTrackService;
import org.springframework.stereotype.Service;

/**
 * 物流轨迹服务实现类
 */
@Service
public class LogisticsTrackServiceImpl extends ServiceImpl<LogisticsTrackMapper, OmsLogisticsTrack>
        implements LogisticsTrackService {

}
