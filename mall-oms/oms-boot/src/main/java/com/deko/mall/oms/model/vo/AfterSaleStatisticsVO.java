package com.deko.mall.oms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 售后统计数据模型
 */
@Schema(description = "售后统计数据")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AfterSaleStatisticsVO {

    @Schema(description = "售后单总数")
    private Long totalCount;


    @Schema(description = "总退款金额")
    private BigDecimal totalRefund;

    @Schema(description = "待审核数量")
    private Long pendingAuditCount;

    @Schema(description = "待收货数量")
    private Long pendingReceiveCount;

    @Schema(description = "退款中数量")
    private Long refundingCount;
}
