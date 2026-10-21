package com.deko.mall.oms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 批量发货结果VO
 */
@Data
@Builder
public class BatchDeliverResultVO {

    @Schema(description = "总数量")
    private int totalCount;

    @Schema(description = "成功数量")
    private int successCount;

    @Schema(description = "失败数量")
    private int failCount;

    @Schema(description = "成功发货的订单编号列表")
    private List<String> successOrderNos;

    @Schema(description = "失败的订单及原因列表")
    private List<FailItem> failItems;

    @Data
    @Builder
    public static class FailItem {
        @Schema(description = "订单编号")
        private String orderNo;

        @Schema(description = "失败原因")
        private String reason;
    }
}

