package com.deko.mall.ums.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 地址表单对象
 */
@Schema(description = "地址表单对象")
@Data
public class AddressForm {

    @Schema(description = "地址ID")
    private Long id;

    @Schema(description = "收货人姓名")
    private String consigneeName;

    @Schema(description = "收货人手机号")
    private String consigneeMobile;

    @Schema(description = "国家区域ID(sys_region level=0)")
    private Integer countryId;

    @Schema(description = "国家名称")
    private String countryName;

    @Schema(description = "省")
    //@CheckCityValid(CityType.PROVINCE)
    private String provinceId;

    @Schema(description = "市")
    //@CheckCityValid(CityType.CITY)
    private String cityId;

    @Schema(description = "区")
    //@CheckCityValid(CityType.AREA)
    private String areaId;

    @Schema(description = "详细地址")
    @Length(min = 1, max = 100, message = "{text.length.min}，{text.length.max}")
    private String detailAddress;

    @Schema(description = "是否默认地址(1:是;0:否)")
    private Integer defaulted;

    @Schema(description = "地址别名")
    private String alias;

    @Schema(description = "经度")
    private java.math.BigDecimal lng;

    @Schema(description = "纬度")
    private java.math.BigDecimal lat;

    private String provinceName;

    private String cityName;

    private String areaName;

}



