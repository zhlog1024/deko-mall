package com.deko.mall.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.deko.mall.oms.model.entity.order.OmsOrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单商品明细表
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OmsOrderItem> {

    List<Long> selectUnsettledOrderIds(@Param("receiveDeadline") LocalDateTime receiveDeadline,
                                       @Param("limit") int limit);
}
