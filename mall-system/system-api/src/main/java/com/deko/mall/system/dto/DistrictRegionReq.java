package com.deko.mall.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class DistrictRegionReq {

    @ApiModelProperty(value = "地址父级ID")
    private List<Long> pidList;
}
