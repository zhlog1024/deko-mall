package com.deko.mall.oms.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 售后申请表单
 */
@Data
@Schema(description = "售后申请表单")
public class OrderAfterSaleForm {

    @NotNull(message = "订单编号不能为空")
    @Schema(description = "订单编号")
    private String orderNo;

    @NotNull(message = "售后类型不能为空")
    @Schema(description = "售后类型(1-仅退款，2-退货退款)")
    private Integer afterSaleType;


    @NotNull(message = "退款金额不能为空")
    @Min(value = 0, message = "退款金额不能为负数")
    @Schema(description = "退款金额(元)")
    private BigDecimal refundAmount;

    @NotBlank(message = "售后原因不能为空")
    @Schema(description = "售后原因")
    private String reason;

    @Schema(description = "问题描述")
    private String description;

    @Schema(description = "图片凭证，多个图片用逗号分隔")
    private String applyPics;

    @Schema(description = "售后商品列表，部分退款时必填")
    private List<OrderAfterSaleItemForm> orderItems;
}
