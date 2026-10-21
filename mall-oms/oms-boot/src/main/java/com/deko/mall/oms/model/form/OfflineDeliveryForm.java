package com.deko.mall.oms.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 线下发货表单
 */
@Data
@Schema(description = "线下发货请求参数")
public class OfflineDeliveryForm {

    @NotNull(message = "订单ID不能为空")
    @Schema(description = "订单ID")
    private Long orderId;

    @NotBlank(message = "发货凭证不能为空")
    @Schema(description = "线下发货凭证URLs，逗号分隔")
    private String offlineProof;

    @Schema(description = "备注/说明（如：同城配送、自提）")
    private String offlineRemark;

    @Schema(description = "物流公司编码（可选）")
    private String carrierCode;

    @Schema(description = "物流公司名称（可选）")
    private String carrierName;

    @Schema(description = "物流单号（可选）")
    private String trackingNo;
}
