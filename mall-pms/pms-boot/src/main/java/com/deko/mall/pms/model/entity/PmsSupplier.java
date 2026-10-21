package com.deko.mall.pms.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 供应商实体
 */
@Data
@TableName("pms_supplier")
@EqualsAndHashCode(callSuper = true)
public class PmsSupplier extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 供应商编码
     */
    private String supplierCode;

    /**
     * 供应商名称
     */
    private String supplierName;


    /**
     * 法人联系电话
     */
    private String loginName;

    /**
     * 营业执照号
     */
    private String businessLicense;


    /**
     * 审核状态(0-待审核,1-审核通过,2-审核拒绝)
     */
    private Integer auditStatus;

    /**
     * 退货收货人
     */
    private String returnContact;

    /**
     * 退货收货电话
     */
    private String returnPhone;

    /**
     * 退货收货地址
     */
    private String returnAddress;

    /**
     * 审核备注
     */
    private String remark;

    /**
     * 店铺名称（入驻时填写，默认取供应商名称）
     */
    private String shopName;

    /**
     * 店铺logo（入驻时填写，审核通过后写入店铺）
     */
    private String shopLogo;

    /**
     * 店铺简介（入驻时填写，选填）
     */
    private String shopDescription;
}
