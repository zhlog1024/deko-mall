package com.deko.mall.oms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Admin-订单分页视图对象
 */
@Data
public class OmsOrderPriceVO {


    @Schema(description = "订单总金额(元)")
    private BigDecimal totalAmount;

    @Schema(description = "订单总金额(元)")
    private BigDecimal paymentAmount;


}
