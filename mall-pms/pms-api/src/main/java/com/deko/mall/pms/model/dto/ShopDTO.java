package com.deko.mall.pms.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 店铺传输对象（供收藏中心等跨服务回查）
 */
@Data
public class ShopDTO {

    private Long id;

    /**
     * 归属类型(SUPPLIER-供应商, MEMBER-会员)
     */
    private String ownerType;

    /**
     * 归属ID(供应商ID或会员ID)
     */
    private Long ownerId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 店铺logo
     */
    private String logo;

    /**
     * 店铺长图
     */
    private String logoLong;

    /**
     * 店铺简介
     */
    private String description;

    /**
     * 店铺状态(0-关闭,1-开启)
     */
    private Integer status;

    private LocalDateTime createTime;
}
