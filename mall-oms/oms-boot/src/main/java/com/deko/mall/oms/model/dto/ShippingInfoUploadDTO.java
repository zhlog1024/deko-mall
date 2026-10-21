package com.deko.mall.oms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 微信发货信息录入参数
 */
@Data
public class ShippingInfoUploadDTO implements Serializable {

    @Schema(description = "商户订单号")
    private String outTradeNo;

    @Schema(description = "用户openid")
    private String openid;

    @Schema(description = "发货描述")
    private String desc;

    @Schema(description = "物流公司编码")
    private String expressCompany;

    @Schema(description = "物流单号")
    private String trackingNo;
}
