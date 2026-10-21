package com.deko.mall.pms.model.form;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品库存单元
 */
@Data
public class PmsSkuForm {

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

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
