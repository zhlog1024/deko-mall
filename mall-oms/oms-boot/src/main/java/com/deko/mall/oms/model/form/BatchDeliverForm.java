package com.deko.mall.oms.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量发货表单对象
 */
@Data
public class BatchDeliverForm {

    @Schema(description = "批量发货详情列表")
    @NotEmpty(message = "发货详情不能为空")
    private List<DeliverItem> items;

    @Data
    public static class DeliverItem {

        @Schema(description = "订单编号", required = true)
        @NotBlank(message = "订单编号不能为空")
        private String orderNo;

        @Schema(description = "物流公司编码", required = true)
        @NotBlank(message = "物流公司编码不能为空")
        private String deliveryCode;

        @Schema(description = "物流单号", required = true)
        @NotBlank(message = "物流单号不能为空")
        private String deliverySn;

        @Schema(description = "发货备注")
        private String remark;
    }
}

