package com.deko.mall.oms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 线下发货申请详情 VO
 */
@Data
@Schema(description = "线下发货申请详情")
public class OfflineDeliveryDetailVO {

    @Schema(description = "申请ID")
    private Long id;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "线下发货凭证URLs，逗号分隔")
    private String offlineProof;

    @Schema(description = "备注/说明")
    private String offlineRemark;

    @Schema(description = "物流公司编码")
    private String carrierCode;

    @Schema(description = "物流公司名称")
    private String carrierName;

    @Schema(description = "物流单号")
    private String trackingNo;

    @Schema(description = "申请时间")
    private LocalDateTime applyTime;

    @Schema(description = "审核状态：1通过")
    private Integer auditStatus;

    @Schema(description = "审核状态名称")
    private String auditStatusLabel;

    @Schema(description = "审核人")
    private String auditor;

    @Schema(description = "审核时间")
    private LocalDateTime auditTime;

    @Schema(description = "审核备注")
    private String auditRemark;
}
