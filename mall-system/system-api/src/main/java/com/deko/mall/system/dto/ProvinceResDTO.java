package com.deko.mall.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 中国行政地区表
 */
@Data
@ApiModel("省行政对象")
public class ProvinceResDTO implements Serializable {

    /**
     * 地址ID
     */
    @ApiModelProperty("地址ID")
    private Long id;

    /**
     * 父级ID
     */
    @ApiModelProperty("地址父级ID")
    private Long parentId;
    /**
     * 层级
     */
    @ApiModelProperty("地址层级 1 省 2 市 3 区 4 镇")
    private Integer level;

    /**
     * 名称
     */
    @ApiModelProperty("地址名称")
    private String name;

    @ApiModelProperty("是否选中：前端使用，默认返回false")
    private Boolean check;

    @ApiModelProperty("是否禁用选中：前端使用，默认返回false")
    private Boolean disabled;

    @ApiModelProperty("市行政集合对象")
    private List<CityResDTO> citys;
}
