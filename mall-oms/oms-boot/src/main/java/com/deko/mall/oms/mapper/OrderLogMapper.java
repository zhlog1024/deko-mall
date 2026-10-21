package com.deko.mall.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.deko.mall.oms.model.entity.order.OmsOrderLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单操作历史记录
 *
 * @author zhlog1024
 */
@Mapper
public interface OrderLogMapper extends BaseMapper<OmsOrderLog> {

}
