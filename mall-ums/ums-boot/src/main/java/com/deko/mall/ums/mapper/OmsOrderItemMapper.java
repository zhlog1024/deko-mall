package com.deko.mall.ums.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 跨模块查询：oms 订单商品行
 * 同库直查模式（ums 现有跨表 SQL 先例），不依赖 oms 实体
 */
@Mapper
public interface OmsOrderItemMapper {

    /**
     * 查询订单的全部商品行ID
     *
     * @param orderId 订单ID
     * @return 商品行ID列表
     */
    @Select("SELECT id FROM oms_order_item WHERE order_id = #{orderId} AND deleted = 0 LIMIT 2")
    List<Long> selectItemIdsByOrderId(@Param("orderId") Long orderId);
}
