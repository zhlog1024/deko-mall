package com.deko.mall.pms.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 店铺视图对象
 */
@Data
@Schema(description = "店铺视图对象")
public class ShopVO {

    @Schema(description = "店铺ID")
    private Long id;

    @Schema(description = "归属类型(SUPPLIER-供应商, MEMBER-会员)")
    private String ownerType;

    @Schema(description = "归属ID(供应商ID或会员ID)")
    private Long ownerId;

    @Schema(description = "店铺名称")
    private String shopName;

    @Schema(description = "店铺logo")
    private String logo;

    @Schema(description = "店铺长图")
    private String logoLong;

    @Schema(description = "店铺简介")
    private String description;

    @Schema(description = "店铺状态(0-关闭,1-开启)")
    private Integer status;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "店铺评分(0-5)")
    private java.math.BigDecimal score;

    @Schema(description = "商品数")
    private Long goodsCount;

    @Schema(description = "店铺收藏数")
    private Long favorCount;

    @Schema(description = "当前会员是否已收藏")
    private Boolean isFavor;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
