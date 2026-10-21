package com.deko.mall.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.deko.mall.oms.model.entity.order.OmsOrderDelivery;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单物流记录表
 *
 * @author zhlog1024
 */
@Mapper
public interface OrderDeliveryMapper extends BaseMapper<OmsOrderDelivery> {

}
