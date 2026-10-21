package com.deko.mall.oms.model.entity.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 订单发货信息
 */
@Data
@Accessors(chain = true)
@Builder
@TableName("oms_order_delivery")
public class OmsOrderDelivery extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 物流公司编码
     */
    private String carrierCode;

    /**
     * 物流单号
     */
    private String trackingNo;

    /**
     * 物流公司
     */
    private String carrierName;

    /**
     * 发货状态(1:待发货 2:已发货 3:已完成)
     */
    private Integer status;

    /**
     * 发货时间
     */
    private LocalDateTime deliveryTime;

    /**
     * 完成时间
     */
    private LocalDateTime completeTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 发货类型：1线上物流 2线下发货
     */
    private Integer deliveryType;

    /**
     * 发货单归属供应商(单供应商订单=订单供应商; 多供应商订单各自发货)
     */
    private Long supplierId;

}