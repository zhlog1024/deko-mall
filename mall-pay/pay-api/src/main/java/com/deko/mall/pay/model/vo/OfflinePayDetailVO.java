package com.deko.mall.pay.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 线下支付凭证详情（Feign / API 层共享）
 */
@Data
public class OfflinePayDetailVO implements Serializable {

    private String paymentNo;
    private String paymentChannelNo;
    private String voucherUrls;
    private Integer auditStatus;
    private String auditRemark;
    private LocalDateTime auditTime;
    private String auditor;
    private Integer resubmitCount;
}
