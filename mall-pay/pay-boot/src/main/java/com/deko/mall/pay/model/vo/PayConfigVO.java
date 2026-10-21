package com.deko.mall.pay.model.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.deko.mall.pay.entity.PayExtConfig;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PayConfigVO {

    private Long id;
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
    private Boolean enabled;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PayExtConfig parsedExtConfig;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
