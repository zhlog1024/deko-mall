package com.deko.mall.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class CityRegionReq {

    @ApiModelProperty(value = "地址父级ID")
    private List<Long> pidList;
}
