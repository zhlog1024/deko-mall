package com.deko.mall.oms.model.query;

import com.deko.mall.common.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 退款分页查询对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "退款分页查询对象")
public class AfterSalePageQuery extends BasePageQuery {

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "售后单号")
    private String afterSaleNo;

    @Schema(description = "退款状态")
    private String status;

    @Schema(description = "售后类型")
    private Integer afterSaleType;

    @Schema(description = "用户ID")
    private Long memberId;

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "开始日期")
    private String beginDate;

    @Schema(description = "结束日期")
    private String endDate;

    @Schema(description = "关键字(售后单号/订单号/商品名称)")
    private String keywords;
}
