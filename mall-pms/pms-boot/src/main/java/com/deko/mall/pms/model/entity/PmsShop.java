package com.deko.mall.pms.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 店铺实体
 */
@Data
@TableName("pms_shop")
@EqualsAndHashCode(callSuper = true)
public class PmsShop extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
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

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 店铺评分(0-5，运营后台维护)
     */
    private java.math.BigDecimal score;
}
