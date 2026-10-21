package com.deko.mall.oms.service.order.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.oms.mapper.OrderSettingMapper;
import com.deko.mall.oms.model.entity.order.OmsOrderSetting;
import com.deko.mall.oms.service.order.OrderSettingService;
import org.springframework.stereotype.Service;


@Service
public class OrderSettingServiceImpl extends ServiceImpl<OrderSettingMapper, OmsOrderSetting> implements OrderSettingService {

}
