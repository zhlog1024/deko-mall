package com.deko.mall.pay.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 支付行为参数
 */
@Data
public class PayActionDTO implements Serializable {

    @Schema(description = "支付渠道")
    private String channel;

    @Schema(description = "支付单号")
    private String paymentNo;

    @Schema(description = "支付授权码（如微信code）")
    private String code;

    @Schema(description = "设备类型")
    private String deviceType;
}
