package com.deko.mall.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum RegionLevelEnum {

    COUNTRY_LEVEL(0, "国家"),
    PROVINCE_LEVEL(1, "省行政"),
    CITY_LEVEL(2, "市行政"),
    DISTRICT_LEVEL(3, "区行政"),
    TOWN_LEVEL(4, "镇行政");

    private int value;

    private String label;

}
