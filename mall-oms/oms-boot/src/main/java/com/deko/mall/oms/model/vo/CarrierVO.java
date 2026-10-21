package com.deko.mall.oms.model.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CarrierVO {
    private String carrierCode;
    private String carrierName;
}