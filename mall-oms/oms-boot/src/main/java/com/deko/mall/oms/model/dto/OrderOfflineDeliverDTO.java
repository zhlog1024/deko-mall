package com.deko.mall.oms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 线下发货保存参数
 */
@Data
public class OrderOfflineDeliverDTO implements Serializable {

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "物流公司编码")
    private String deliveryCode;

    @Schema(description = "物流单号")
    private String deliverySn;

    @Schema(description = "物流公司名称")
    private String carrierName;
}
