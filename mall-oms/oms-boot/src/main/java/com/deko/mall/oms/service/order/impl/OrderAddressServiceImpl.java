package com.deko.mall.oms.service.order.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.oms.enums.DataDeletedEnum;
import com.deko.mall.oms.enums.ReceiveAddressTypeEnum;
import com.deko.mall.oms.mapper.OrderAddressMapper;
import com.deko.mall.oms.model.entity.order.OmsOrderAddress;
import com.deko.mall.oms.service.order.OrderAddressService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author zhlog1024
 * @create
 */
@Component
public class OrderAddressServiceImpl extends ServiceImpl<OrderAddressMapper, OmsOrderAddress> implements OrderAddressService {

    public List<OmsOrderAddress> selectList(List<Long> orderIdList, ReceiveAddressTypeEnum receiveAddressType) {
        return getBaseMapper().selectList(
                new QueryWrapper<OmsOrderAddress>()
                        .lambda()
                        .in(OmsOrderAddress::getOrderId, orderIdList)
                        .eq(OmsOrderAddress::getReceiveAddressType, receiveAddressType)
                        .eq(OmsOrderAddress::getDeleted, DataDeletedEnum.ZERO)
        );
    }


    public OmsOrderAddress findByOrderId(Long orderId, ReceiveAddressTypeEnum receiveAddressType) {
        return getBaseMapper().selectOne(
                new QueryWrapper<OmsOrderAddress>()
                        .lambda()
                        .eq(OmsOrderAddress::getOrderId, orderId)
                        .eq(OmsOrderAddress::getDeleted, DataDeletedEnum.ZERO)
                        .eq(OmsOrderAddress::getReceiveAddressType, receiveAddressType)
        );
    }


    public OmsOrderAddress findByOrderItemId(Long orderItemId, ReceiveAddressTypeEnum receiveAddressType) {
        return getBaseMapper().selectOne(
                new QueryWrapper<OmsOrderAddress>()
                        .lambda()
                        .eq(OmsOrderAddress::getOrderItemId, orderItemId)
                        .eq(OmsOrderAddress::getDeleted, DataDeletedEnum.ZERO)
                        .eq(OmsOrderAddress::getReceiveAddressType, receiveAddressType)
        );
    }


    public Boolean deleteByOrderItemId(Long orderItemId, ReceiveAddressTypeEnum receiveAddressType) {
        return lambdaUpdate().eq(OmsOrderAddress::getOrderItemId, orderItemId).
                eq(OmsOrderAddress::getReceiveAddressType, receiveAddressType.getCode())
                .set(OmsOrderAddress::getDeleted, DataDeletedEnum.ONE.getValue()).set(OmsOrderAddress::getUpdateTime, LocalDateTime.now())
                .update();
    }


    public Boolean updateUserPhone(Long userId, String loginName) {
        return lambdaUpdate()
                .eq(OmsOrderAddress::getUserId, userId)
                .set(OmsOrderAddress::getUserPhone, loginName)
                .update();
    }
}