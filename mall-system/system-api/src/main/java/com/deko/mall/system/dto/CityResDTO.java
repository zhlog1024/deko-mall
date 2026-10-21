package com.deko.mall.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 中国行政地区表
 * @CreateDate: 2022/9/1 15:16
 */
@Data
@ApiModel("市行政对象")
public class CityResDTO {

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

}
