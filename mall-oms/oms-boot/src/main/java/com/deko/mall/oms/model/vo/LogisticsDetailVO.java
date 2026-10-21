package com.deko.mall.oms.model.vo;

import com.deko.mall.oms.model.entity.logistics.OmsLogisticsTrack;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 物流详情视图对象
 */
@Schema(description = "物流详情")
@Data
public class LogisticsDetailVO {

    @Schema(description = "业务单号")
    private String bizNo;


    @Schema(description = "物流类型")
    private Integer logisticsType;

    @Schema(description = "物流类型")
    private String logisticsTypeLabel;

    @Schema(description = "物流单号")
    private String trackingNo;

    @Schema(description = "物流公司编码")
    private String carrierCode;

    @Schema(description = "物流公司名称")
    private String carrierName;

    @Schema(description = "物流状态")
    private Integer status;

    @Schema(description = "物流状态描述")
    private String statusDesc;

    @Schema(description = "是否线下发货")
    private Boolean offlineDelivery;

    @Schema(description = "线下发货备注")
    private String offlineRemark;

    @Schema(description = "物流轨迹")
    private List<OmsLogisticsTrack> tracks;
}
