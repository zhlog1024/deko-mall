package com.deko.mall.ums.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 智能地址识别结果
 */
@Data
@Schema(description = "智能地址识别结果")
public class AddressParseRes {

    @Schema(description = "收货人姓名")
    @JsonProperty("name")
    private String consigneeName;
    @Schema(description = "收货人手机号")
    @JsonProperty("tel")
    private String consigneeMobile;
    @Schema(description = "省ID")
    @JsonProperty("province")
    private Long provinceId;
    @Schema(description = "省名")
    @JsonProperty("province_name")
    private String provinceName;
    @Schema(description = "市ID")
    @JsonProperty("city")
    private Long cityId;
    @Schema(description = "市名")
    @JsonProperty("city_name")
    private String cityName;
    @Schema(description = "区ID")
    @JsonProperty("county")
    private Long areaId;
    @Schema(description = "区名")
    @JsonProperty("county_name")
    private String areaName;
    @Schema(description = "详细地址")
    @JsonProperty("address")
    private String detailAddress;
}
