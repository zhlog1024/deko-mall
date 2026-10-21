package com.deko.mall.ums.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;

@Data
public class UmsAddress extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会员ID
     */
    private Long memberId;

    /** 国家区域ID(sys_region.level=0) */
    private Integer countryId;

    /** 国家名称 */
    private String countryName;

    /**
     * 收货人姓名
     */
    private String consigneeName;

    /**
     * 收货人联系方式
     */
    private String consigneeMobile;

    private Integer provinceId;
    private String provinceName;

    private Integer cityId;
    private String cityName;

    private Integer areaId;
    private String areaName;

    private String detailAddress;
    private String completeAddress;

    /**
     * 邮编
     */
    private String zipCode;

    /**
     * 是否默认地址(1:是；0:否)
     */
    private Integer defaulted;

    /**
     * 地址别名（家/公司/学校/自定义）
     */
    private String alias;

    /**
     * 经度
     */
    private java.math.BigDecimal lng;

    /**
     * 纬度
     */
    private java.math.BigDecimal lat;

    private Integer deleted;

}
