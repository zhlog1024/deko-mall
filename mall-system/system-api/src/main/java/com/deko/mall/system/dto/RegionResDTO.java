package com.deko.mall.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @CreateDate: 2022/9/2 14:28
 */
@Data
public class RegionResDTO {

    List<RegionResDTO> children;
    @ApiModelProperty("地址ID")
    private Long id;
    @ApiModelProperty("地址父级ID")
    private Long parentId;
    /**
     * 名称
     */
    @ApiModelProperty("地址名称")
    private String name;
    @ApiModelProperty("地址层级 1 省 2 市 3 区 4 镇")
    private Integer level;
    @ApiModelProperty("是否显示")
    private Integer hidden;

}

