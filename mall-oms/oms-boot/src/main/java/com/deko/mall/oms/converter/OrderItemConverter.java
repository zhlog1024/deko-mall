package com.deko.mall.oms.converter;

import com.deko.mall.oms.model.dto.OrderItemDTO;
import com.deko.mall.oms.model.entity.order.OmsOrderItem;
import com.deko.mall.oms.model.form.OrderSubmitForm;
import com.deko.mall.oms.model.vo.OrderItemVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;


/**
 * 订单商品明细对象转化器
 */
@Mapper(componentModel = "spring")
public interface OrderItemConverter {

    @Mappings({
            @Mapping(target = "totalAmount", expression = "java(item.getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))"),
    })
    OmsOrderItem item2Entity(OrderSubmitForm.OrderItem item);


    OrderItemVO dto2VO(OrderItemDTO orderItemDTO);

    List<OrderItemVO> dto2ListVO(List<OrderItemDTO> orderItemDTOs);


    List<OmsOrderItem> item2Entity(List<OrderSubmitForm.OrderItem> list);

}