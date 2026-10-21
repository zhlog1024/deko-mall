package com.deko.mall.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 中国行政地区表
 */
@Data
@ApiModel("镇行政对象")
public class TownResDTO implements Serializable {

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

}
