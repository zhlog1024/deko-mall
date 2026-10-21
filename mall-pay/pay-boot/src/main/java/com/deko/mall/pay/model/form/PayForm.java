package com.deko.mall.pay.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 支付表单
 */
@Schema(description = "支付表单")
@Data
public class PayForm {

    @Schema(description = "支付单号（统一字段，paymentNo）")
    @NotBlank(message = "支付单号不能为空")
    private String paymentNo;

    @Schema(description = "支付渠道")
    @NotBlank(message = "支付渠道不能为空")
    private String payChannel;

    @Schema(description = "设备类型")
    @NotBlank(message = "设备类型不能为空")
    private String deviceType;

    /** 微信认证code */
    @Schema(description = "微信认证code")
    private String code;

    /** 线下支付凭证图片URL列表（仅OFFLINE_PAY使用） */
    @Schema(description = "线下支付凭证URL列表")
    private java.util.List<String> voucherUrls;

}
