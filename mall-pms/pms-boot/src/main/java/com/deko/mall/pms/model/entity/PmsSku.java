package com.deko.mall.pms.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品库存单元
 */
@Data
@TableName("pms_sku")
@EqualsAndHashCode(callSuper = true)
public class PmsSku extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 库存单元编号
     */
    private String skuSn;

    /**
     * SKU 名称
     */
    private String name;

    /**
     * SPU ID
     */
    private Long spuId;

    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 规格ID，多个使用英文逗号(,)分割
     */
    private String specIds;

    /**
     * 供货价(单位：元)
     */
    private BigDecimal supplyPrice;

    /**
     * 采购价(单位：元)，仅用于商家端和运营后台内部展示，不对前端暴露
     */
    private BigDecimal purchasePrice;

    /**
     * 划线价(单位：元)
     */
    private BigDecimal marketPrice;

    /**
     * 零售价(单位：元)
     */
    private BigDecimal retailPrice;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 锁定库存数量
     */
    private Integer lockedStock;

    /**
     * 商品图片地址
     */
    private String picUrl;

    /**
     * 逻辑删除标识：0-未删除，1-已删除
     */
    @TableLogic
    private Integer deleted;

}
