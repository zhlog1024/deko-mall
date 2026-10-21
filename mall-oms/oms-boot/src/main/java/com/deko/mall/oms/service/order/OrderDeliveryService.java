package com.deko.mall.oms.service.order;

import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.oms.model.entity.order.OmsOrderDelivery;
import com.deko.mall.oms.model.dto.OrderOfflineDeliverDTO;

/**
 * 订单物流记录表服务接口
 */
public interface OrderDeliveryService extends IService<OmsOrderDelivery> {

    /**
     * 发货单保存
     *
     * @param orderId      订单ID
     * @param supplierId   发货供应商ID（多供应商订单各自发货）
     * @param deliveryCode 物流公司编码
     * @param deliverySn   物流单号
     * @return 是否发货成功
     */
    boolean saveOrderDelivery(Long orderId, Long supplierId, String deliveryCode, String deliverySn);

    /**
     * 线下发货保存
     *
     * @param orderId      订单ID
     * @param supplierId   发货供应商ID
     * @param deliveryCode 物流公司编码
     * @param deliverySn   物流单号
     * @param carrierName  物流公司名称
     * @return 是否保存成功
     */
    boolean saveOrderOfflineDeliver(OrderOfflineDeliverDTO dto);

    /**
     * 更新物流状态
     *
     * @param orderId 订单号
     * @param status  物流状态
     * @return 是否更新成功
     */
    boolean updateDeliveryStatus(Long orderId, Integer status);

    /**
     * 获取订单物流信息
     *
     * @param orderId 订单ID
     * @return 订单物流信息
     */
    OmsOrderDelivery getOrderDelivery(Long orderId);

    /**
     * 获取订单指定供应商的发货单（多供应商订单各自发货的校验键）
     *
     * @param orderId    订单ID
     * @param supplierId 供应商ID
     * @return 发货单（无则 null）
     */
    OmsOrderDelivery getOrderDeliveryBySupplier(Long orderId, Long supplierId);

    /**
     * 按物流单号获取发货单（签收定位用）
     *
     * @param trackingNo 物流单号
     * @return 发货单（无则 null）
     */
    OmsOrderDelivery getOrderDeliveryByTrackingNo(String trackingNo);
}
