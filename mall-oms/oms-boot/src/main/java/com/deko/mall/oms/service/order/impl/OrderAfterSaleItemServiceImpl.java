package com.deko.mall.oms.service.order.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.oms.mapper.OrderAfterSaleItemMapper;
import com.deko.mall.oms.model.entity.order.OmsOrderAfterSaleItem;
import com.deko.mall.oms.service.order.OrderAfterSaleItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 订单售后商品明细服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderAfterSaleItemServiceImpl extends ServiceImpl<OrderAfterSaleItemMapper, OmsOrderAfterSaleItem> implements OrderAfterSaleItemService {

    private final OrderAfterSaleItemMapper orderAfterSaleItemMapper;

    @Override
    public List<OmsOrderAfterSaleItem> getByAfterSaleId(Long afterSaleId) {
        LambdaQueryWrapper<OmsOrderAfterSaleItem> queryWrapper = new LambdaQueryWrapper<OmsOrderAfterSaleItem>().eq(OmsOrderAfterSaleItem::getAfterSaleId, afterSaleId);
        return orderAfterSaleItemMapper.selectList(queryWrapper);
    }

    @Override
    public boolean saveBatch(List<OmsOrderAfterSaleItem> items) {
        if (items == null || items.isEmpty()) {
            return false;
        }
        return super.saveBatch(items);
    }
}
