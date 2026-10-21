package com.deko.mall.oms.model.entity.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 订单发货单商品明细
 * <p>
 * 发货单↔商品项关联（一个发货单可含同供应商多个商品项；多供应商订单各自发货）
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("oms_order_delivery_item")
public class OmsOrderDeliveryItem extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发货单ID(oms_order_delivery.id)
     */
    private Long deliveryId;

    /**
     * 订单商品项ID(oms_order_item.id)
     */
    private Long orderItemId;

    /**
     * 本发货单中的商品数量
     */
    private Integer quantity;

}
