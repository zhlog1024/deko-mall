package com.deko.mall.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.deko.mall.oms.model.entity.order.OmsOrderSetting;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单配置信息
 *
 * @author zhlog1024
 */
@Mapper
public interface OrderSettingMapper extends BaseMapper<OmsOrderSetting> {

}
