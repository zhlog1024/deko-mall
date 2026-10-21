package com.deko.mall.pms.model.query;

import com.deko.mall.common.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 结算单分页查询参数
 */
@Data
@Schema(description = "结算单分页查询参数")
@EqualsAndHashCode(callSuper = true)
public class SettlementRecordPageQuery extends BasePageQuery {

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "结算单号")
    private String settlementNo;

    @Schema(description = "结算状态(0-待确认,1-已确认,2-已打款)")
    private Integer status;


    @Schema(description = "开始时间")
    private String beginTime;

    @Schema(description = "结束时间")
    private String endTime;
}
