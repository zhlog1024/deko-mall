package com.deko.mall.oms.service.order.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.oms.mapper.OrderLogMapper;
import com.deko.mall.oms.model.entity.order.OmsOrderLog;
import com.deko.mall.oms.service.order.OrderLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderLogServiceImpl extends ServiceImpl<OrderLogMapper, OmsOrderLog> implements OrderLogService {

}
