package com.deko.mall.oms.mapper.order;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.deko.mall.oms.model.entity.order.OmsOrderDeliveryItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单发货单商品明细 Mapper
 */
@Mapper
public interface OmsOrderDeliveryItemMapper extends BaseMapper<OmsOrderDeliveryItem> {
}
