package com.deko.mall.oms.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 订单发货DTO
 */
@Data
@Schema(description = "订单发货请求参数")
public class OrderDeliveryForm {

    @NotNull(message = "订单编号不能为空")
    @Schema(description = "订单编号")
    private String orderNo;

    @NotBlank(message = "物流公司不能为空")
    @Schema(description = "物流公司编码")
    private String deliveryCode;

    @NotBlank(message = "物流单号不能为空")
    @Schema(description = "物流单号")
    private String deliverySn;

    @Schema(description = "备注信息")
    private String remark;
}
