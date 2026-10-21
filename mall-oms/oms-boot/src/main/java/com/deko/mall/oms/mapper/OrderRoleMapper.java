package com.deko.mall.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.deko.mall.oms.model.entity.order.OmsOrderRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单营销角色快照Mapper接口
 */
@Mapper
public interface OrderRoleMapper extends BaseMapper<OmsOrderRole> {
}
