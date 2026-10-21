package com.deko.mall.pms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 店铺信息更新参数
 */
@Data
public class ShopUpdateDTO implements Serializable {

    @Schema(description = "店铺ID")
    private Long shopId;

    @Schema(description = "店铺名称")
    private String shopName;

    @Schema(description = "Logo")
    private String logo;

    @Schema(description = "长图Logo")
    private String logoLong;

    @Schema(description = "店铺简介")
    private String description;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "店铺状态(0-关闭,1-开启)")
    private Integer status;

    @Schema(description = "店铺评分(0-5)")
    private BigDecimal score;
}
