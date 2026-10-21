package com.deko.mall.oms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单积分信息VO
 */
@Schema(description = "订单积分信息VO")
@Data
public class OrderPointsInfoVO {

    /**
     * 积分余额
     */
    @Schema(description = "积分余额")
    private BigDecimal pointsBalance;


    /**
     * 建议使用的积分金额（元）
     */
    @Schema(description = "建议使用的积分金额（元）")
    private BigDecimal suggestedPoints;





}
