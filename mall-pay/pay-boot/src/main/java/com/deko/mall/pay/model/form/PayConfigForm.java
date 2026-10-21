package com.deko.mall.pay.model.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PayConfigForm {

    private Long id;

    @NotNull(message = "支付渠道不能为空")
    private String payChannel;
    private String appId;
    private String mchId;
    private String paymentNotifyUrl;
    private String refundNotifyUrl;
    private String deviceType;
    private String channelName;
    private String cashierName;
    private String channelIcon;
    private String allowedUserIds;
    private String extConfig;
    @NotNull(message = "启用状态不能为空")
    private Boolean enabled;
}
