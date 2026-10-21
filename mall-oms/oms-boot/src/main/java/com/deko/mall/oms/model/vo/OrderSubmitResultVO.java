package com.deko.mall.oms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单提交结果视图对象
 */
@Schema(description = "订单提交结果视图对象")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSubmitResultVO {

    @Schema(description = "订单号（单订单时为订单号，多订单时为父订单号）")
    private String orderNo;

    @Schema(description = "支付单号（用于调用收银台接口）")
    private String paymentNo;

    @Schema(description = "是否需要支付")
    private Boolean needPay;

    @Schema(description = "应付金额（单位：元）")
    private BigDecimal paymentAmount;

    @Schema(description = "子订单数量（多供应商订单）")
    private Integer subOrderCount;
}
