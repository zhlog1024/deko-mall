package com.deko.mall.oms.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 退款结果数据对象
 */
@Data
public class RefundResultVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 售后单ID
     */
    private Long afterSaleId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 退款结果：true成功，false失败
     */
    private Boolean success;

    /**
     * 退款时间
     */
    private LocalDateTime refundTime;

    /**
     * 退款流水号
     */
    private String refundNo;

    /**
     * 失败原因（如果失败）
     */
    private String failReason;
}
