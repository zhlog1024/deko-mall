package com.deko.mall.oms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 订单发货导入结果
 */
@Data
@Accessors(chain = true)
@Schema(description = "订单发货导入结果")
public class OrderDeliveryImportVO {

    @Schema(description = "总数")
    private Integer total;

    @Schema(description = "成功数")
    private Integer successCount;

    @Schema(description = "失败数")
    private Integer failCount;

    @Schema(description = "失败记录")
    private List<FailureRecord> failureRecords;

    @Data
    @Schema(description = "失败记录")
    public static class FailureRecord {

        @Schema(description = "行号")
        private Integer rowNum;

        @Schema(description = "订单号")
        private String orderSn;

        @Schema(description = "失败原因")
        private String reason;
    }
}
