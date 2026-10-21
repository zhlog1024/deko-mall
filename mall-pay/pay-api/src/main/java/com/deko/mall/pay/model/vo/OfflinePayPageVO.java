package com.deko.mall.pay.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 线下支付审核列表 VO
 */
@Data
public class OfflinePayPageVO implements Serializable {

    private Long id;
    private String paymentNo;
    private String voucherUrls;
    private Integer auditStatus;
    private String auditRemark;
    private LocalDateTime auditTime;
    private String auditor;
    private Integer resubmitCount;
    private LocalDateTime createTime;

    /** 支付金额 (来自 pay_payment) */
    private BigDecimal payAmount;
    /** 会员ID (来自 pay_payment) */
    private Long memberId;
}
