package com.deko.mall.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deko.mall.oms.model.bo.OrderBO;
import com.deko.mall.oms.model.entity.order.OmsOrder;
import com.deko.mall.oms.model.query.OrderPageQuery;
import com.deko.mall.oms.model.vo.OmsOrderPriceVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单数据访问层
 */
@Mapper
public interface OrderMapper extends BaseMapper<OmsOrder> {

    /**
     * 订单分页列表
     */
    Page<OrderBO> getOrderPage(Page<OrderBO> page, @Param("queryParams") OrderPageQuery queryParams);

    OmsOrderPriceVO getOrderPriceSum(@Param("queryParams") OrderPageQuery queryParams);

    List<OrderBO> listOrderPages(@Param("queryParams") OrderPageQuery queryParams);
}
