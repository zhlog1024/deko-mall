package com.deko.mall.oms.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 售后商品项表单
 */
@Data
@Schema(description = "售后商品项表单")
public class OrderAfterSaleItemForm {

    @NotNull(message = "订单商品ID不能为空")
    @Schema(description = "订单商品ID")
    private Long orderItemId;

    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID")
    private Long spuId;

    @NotNull(message = "SKU ID不能为空")
    @Schema(description = "SKU ID")
    private Long skuId;

    @Schema(description = "商品名称")
    private String skuName;

    @NotNull(message = "申请数量不能为空")
    @Min(value = 1, message = "申请数量必须大于0")
    @Schema(description = "申请数量")
    private Integer quantity;

    @NotNull(message = "退款金额不能为空")
    @Min(value = 1, message = "退款金额必须大于0")
    @Schema(description = "退款金额(元)")
    private BigDecimal totalAmount;

    @Schema(description = "商品图片")
    private String picUrl;

    @Schema(description = "商品单价(元)")
    private BigDecimal price;

}
