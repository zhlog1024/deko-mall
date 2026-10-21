package com.deko.mall.oms.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deko.mall.oms.model.bo.OrderBO;
import com.deko.mall.oms.model.dto.OrderDTO;
import com.deko.mall.oms.model.dto.OrderItemDTO;
import com.deko.mall.oms.model.entity.order.OmsOrder;
import com.deko.mall.oms.model.entity.order.OmsOrderItem;
import com.deko.mall.oms.model.form.OrderSubmitForm;
import com.deko.mall.oms.model.vo.OmsOrderPageVO;
import com.deko.mall.oms.model.vo.OrderPageVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

/**
 * 订单对象转化器
 */
@Mapper(componentModel = "spring")
public interface OrderConverter {

    @Mappings({
            @Mapping(target = "orderNo", source = "orderToken"),
            @Mapping(target = "totalQuantity",
                    expression = "java(orderSubmitForm.getOrderItems().stream().map(OrderSubmitForm.OrderItem::getQuantity).reduce(0, Integer::sum))"),
            @Mapping(target = "totalAmount",
                    expression = "java(orderSubmitForm.getOrderItems().stream().map(item -> item.getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity()))).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add))"),
            @Mapping(target = "source", expression = "java(orderSubmitForm.getOrderSource().getValue())"),
    })
    OmsOrder form2Entity(OrderSubmitForm orderSubmitForm);

    @Mappings({
            @Mapping(
                    target = "payChannelLabel",
                    expression = "java(com.deko.mall.common.base.IBaseEnum.getLabelByValue(bo.getPayChannel(), com.deko.mall.pay.api.enums.PayChannelEnum.class))"
            ),
            @Mapping(
                    target = "sourceLabel",
                    expression = "java(com.deko.mall.common.base.IBaseEnum.getLabelByValue(bo.getSource(), com.deko.mall.oms.enums.OrderSourceEnum.class))"
            ),
            @Mapping(
                    target = "statusLabel",
                    expression = "java(com.deko.mall.common.base.IBaseEnum.getLabelByValue(bo.getStatus(), com.deko.mall.oms.enums.OrderStatusEnum.class))"
            ),
            @Mapping(
                    target = "orderTypeLabel",
                    expression = "java(com.deko.mall.common.base.IBaseEnum.getLabelByValue(bo.getOrderType(), com.deko.mall.oms.enums.OrderTypeEnum.class))"
            ),
            @Mapping(
                    target = "orderItems",
                    source = "orderItems"
            ),
            @Mapping(target = "receiverName", source = "receiverName"),
            @Mapping(target = "receiverPhone", source = "receiverPhone"),
            @Mapping(target = "receiverProvince", source = "receiverProvince"),
            @Mapping(target = "receiverCity", source = "receiverCity"),
            @Mapping(target = "receiverDistrict", source = "receiverDistrict"),
            @Mapping(target = "receiverAddress", source = "receiverAddress"),
            @Mapping(target = "receiverCompleteAddress", source = "receiverCompleteAddress"),
            @Mapping(target = "trackingNo", source = "trackingNo"),
            @Mapping(target = "carrierName", source = "carrierName"),
            @Mapping(target = "buyerPhone", source = "buyerPhone"),
            @Mapping(target = "orderScene", source = "orderScene")
    })
    OmsOrderPageVO toVoPage(OrderBO bo);

    Page<OmsOrderPageVO> toVoPage(Page<OrderBO> boPage);

    OmsOrderPageVO.OrderItem toVoPageOrderItem(OrderBO.OrderItem orderItem);


    @Mappings({
            @Mapping(target = "orderType", source = "orderType"),
            @Mapping(
                    target = "payChannelLabel",
                    expression = "java(com.deko.mall.common.base.IBaseEnum.getLabelByValue(bo.getPayChannel(), com.deko.mall.pay.api.enums.PayChannelEnum.class))"
            ),
            @Mapping(
                    target = "sourceLabel",
                    expression = "java(com.deko.mall.common.base.IBaseEnum.getLabelByValue(bo.getSource(), com.deko.mall.oms.enums.OrderSourceEnum.class))"
            ),
            @Mapping(
                    target = "statusLabel",
                    expression = "java(com.deko.mall.common.base.IBaseEnum.getLabelByValue(bo.getStatus(), com.deko.mall.oms.enums.OrderStatusEnum.class))"
            ),
            @Mapping(
                    target = "orderTypeLabel",
                    expression = "java(com.deko.mall.common.base.IBaseEnum.getLabelByValue(bo.getOrderType(), com.deko.mall.oms.enums.OrderTypeEnum.class))"
            ),
            @Mapping(
                    target = "orderItems",
                    source = "orderItems"
            )
    })
    OrderPageVO toVoPageForApp(OrderBO bo);

    Page<OrderPageVO> toVoPageForApp(Page<OrderBO> boPage);

    OrderPageVO.OrderItem toVoPageOrderItemForApp(OrderBO.OrderItem orderItem);

    OrderPageVO toVoApp(OmsOrder orderItem);

    /**
     * 将订单实体转换为OrderDTO
     *
     * @param order 订单实体
     * @return OrderDTO对象
     */
    @Mappings({
            @Mapping(target = "status", source = "status"),
            @Mapping(target = "payChannel", source = "payChannel"),
            @Mapping(target = "orderItems", ignore = true)
    })
    OrderDTO toOrderDTO(OmsOrder order);

    /**
     * 批量将订单实体转换为OrderDTO列表
     *
     * @param orders 订单实体列表
     * @return OrderDTO对象列表
     */
    List<OrderDTO> toOrderDTOList(List<OmsOrder> orders);

    /**
     * 将订单项实体转换为OrderItemDTO
     *
     * @param orderItem 订单项实体
     * @return OrderItemDTO对象
     */
    @Mappings({
            @Mapping(target = "productAmount", source = "totalAmount")
    })
    OrderItemDTO toOrderItemDTO(OmsOrderItem orderItem);

    /**
     * 批量将订单项实体转换为OrderItemDTO列表
     *
     * @param orderItems 订单项实体列表
     * @return OrderItemDTO对象列表
     */
    List<OrderItemDTO> toOrderItemDTOList(List<OmsOrderItem> orderItems);
}