package com.deko.mall.oms.service.order.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.common.base.IBaseEnum;
import com.deko.mall.common.web.exception.BizException;
import com.deko.mall.oms.config.OmsResultCode;
import com.deko.mall.oms.enums.DeliveryStatusEnum;
import com.deko.mall.oms.enums.LogisticsCarrierEnum;
import com.deko.mall.oms.enums.LogisticsStatusEnum;
import com.deko.mall.oms.mapper.OrderDeliveryMapper;
import com.deko.mall.oms.model.entity.order.OmsOrderDelivery;
import com.deko.mall.oms.service.order.OrderDeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import com.deko.mall.oms.model.dto.OrderOfflineDeliverDTO;

/**
 * 订单物流记录表服务实现类
 */
@Slf4j
@Service("orderDeliveryService")
@RequiredArgsConstructor
public class OrderDeliveryServiceImpl extends ServiceImpl<OrderDeliveryMapper, OmsOrderDelivery> implements OrderDeliveryService {


    @Override
    public boolean saveOrderDelivery(Long orderId, Long supplierId, String deliveryCode, String deliverySn) {

        // 订单发货校验：按 (订单, 供应商) 维度，多供应商订单各自发货
        OmsOrderDelivery delivery = this.getOrderDeliveryBySupplier(orderId, supplierId);

        if (delivery != null) {
            throw new BizException(OmsResultCode.ORDER_ALREADY_SHIPPED);
        }

        // 保存物流信息
        delivery = OmsOrderDelivery.builder()
                .orderId(orderId)
                .supplierId(supplierId)
                .carrierCode(deliveryCode)
                .trackingNo(deliverySn)
                .carrierName(IBaseEnum.getLabelByValue(deliveryCode, LogisticsCarrierEnum.class))
                .deliveryTime(LocalDateTime.now())
                .status(DeliveryStatusEnum.DELIVERED.getValue())  // 设置初始状态
                .deliveryType(1)  // 线上物流发货
                .build();
        return this.save(delivery);
    }

    @Override
    public boolean saveOrderOfflineDeliver(OrderOfflineDeliverDTO dto) {

        // 订单发货校验：按 (订单, 供应商) 维度
        OmsOrderDelivery delivery = this.getOrderDeliveryBySupplier(dto.getOrderId(), dto.getSupplierId());

        if (delivery != null) {
            throw new BizException(OmsResultCode.ORDER_ALREADY_SHIPPED);
        }

        // 保存线下发货信息
        delivery = OmsOrderDelivery.builder()
                .orderId(dto.getOrderId())
                .supplierId(dto.getSupplierId())
                .carrierCode(dto.getDeliveryCode())
                .trackingNo(dto.getDeliverySn())
                .carrierName(dto.getCarrierName())
                .deliveryTime(LocalDateTime.now())
                .status(DeliveryStatusEnum.DELIVERED.getValue())  // 设置初始状态
                .deliveryType(2)  // 线下发货
                .build();
        return this.save(delivery);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDeliveryStatus(Long orderId, Integer status) {
        // 更新物流信息
        OmsOrderDelivery delivery = this.lambdaQuery()
                .eq(OmsOrderDelivery::getOrderId, orderId)
                .one();
        if (delivery == null) {
            throw new BizException(OmsResultCode.ORDER_LOGISTICS_NOT_EXIST);
        }

        delivery.setStatus(status);
        // 如果是已签收状态，设置签收时间
        if (LogisticsStatusEnum.SIGNED.getValue().equals(status)) {
            delivery.setCompleteTime(LocalDateTime.now());
        }

        return this.updateById(delivery);
    }

    @Override
    public OmsOrderDelivery getOrderDelivery(Long orderId) {
        if (orderId == null) {
            throw new BizException(OmsResultCode.ORDER_ID_CANNOT_BE_EMPTY);
        }

        return this.getOne(
                new LambdaQueryWrapper<OmsOrderDelivery>()
                        .eq(OmsOrderDelivery::getOrderId, orderId)
                        .last("LIMIT 1")
        );
    }

    @Override
    public OmsOrderDelivery getOrderDeliveryBySupplier(Long orderId, Long supplierId) {
        return this.getOne(
                new LambdaQueryWrapper<OmsOrderDelivery>()
                        .eq(OmsOrderDelivery::getOrderId, orderId)
                        .eq(OmsOrderDelivery::getSupplierId, supplierId)
                        .last("LIMIT 1")
        );
    }

    @Override
    public OmsOrderDelivery getOrderDeliveryByTrackingNo(String trackingNo) {
        return this.getOne(
                new LambdaQueryWrapper<OmsOrderDelivery>()
                        .eq(OmsOrderDelivery::getTrackingNo, trackingNo)
                        .last("LIMIT 1")
        );
    }
}
