package com.deko.mall.oms.service.order;

import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.oms.enums.ReceiveAddressTypeEnum;
import com.deko.mall.oms.model.entity.order.OmsOrderAddress;

import java.util.List;

/**
 * Admin-订单业务接口
 */
public interface OrderAddressService extends IService<OmsOrderAddress> {


    public List<OmsOrderAddress> selectList(List<Long> orderIdList, ReceiveAddressTypeEnum receiveAddressType);


    public OmsOrderAddress findByOrderId(Long orderId, ReceiveAddressTypeEnum receiveAddressType);


    public OmsOrderAddress findByOrderItemId(Long orderItemId, ReceiveAddressTypeEnum receiveAddressType);


    public Boolean deleteByOrderItemId(Long orderItemId, ReceiveAddressTypeEnum receiveAddressType);


    public Boolean updateUserPhone(Long userId, String loginName);

}

