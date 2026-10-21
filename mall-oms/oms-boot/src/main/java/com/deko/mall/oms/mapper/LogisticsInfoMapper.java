package com.deko.mall.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.deko.mall.oms.model.entity.logistics.OmsLogisticsInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 物流运单 Mapper 接口
 */
@Mapper
public interface LogisticsInfoMapper extends BaseMapper<OmsLogisticsInfo> {
}
