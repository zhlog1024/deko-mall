package com.deko.mall.ums.dto;

import lombok.Data;

/**
 * 会员地址传输层对象
 */
@Data
public class MemberAddressDTO {

    private Long id;

    private Long memberId;

    private Integer countryId;
    private String countryName;

    private String consigneeName;

    private String consigneeMobile;

    private Integer provinceId;
    private String provinceName;

    private Integer cityId;
    private String cityName;

    private Integer areaId;
    private String areaName;

    private String detailAddress;
    private String completeAddress;

    private Integer defaulted;

    private String alias;

    private java.math.BigDecimal lng;

    private java.math.BigDecimal lat;

}



