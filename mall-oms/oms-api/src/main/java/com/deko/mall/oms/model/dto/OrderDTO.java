package com.deko.mall.oms.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单DTO
 */
@Data
@Accessors(chain = true)
public class OrderDTO {
    private Long id;

    private String orderNo;

    private BigDecimal totalAmount;

    private BigDecimal paymentAmount;

    private String payChannel;

    private Integer status;

    private String remark;

    private Long memberId;

    private Long supplierId;

    private String orderScene;





    private Long couponId;

    private BigDecimal couponAmount;

    private BigDecimal freightAmount;

    private BigDecimal usePointsAmount;

    private BigDecimal pointsDeductionAmount;





    private LocalDateTime paymentTime;

    private LocalDateTime deliveryTime;

    private LocalDateTime receiveTime;

    private List<OrderItemDTO> orderItems;

    private Integer refundStatus;


    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
