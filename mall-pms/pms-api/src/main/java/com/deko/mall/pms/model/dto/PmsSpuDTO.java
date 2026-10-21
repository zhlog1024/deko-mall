package com.deko.mall.pms.model.dto;

import com.deko.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 商品实体
 */
@Data
@Accessors(chain = true)
public class PmsSpuDTO extends BaseEntity {

    private Long id;

    private String name;

    private Long categoryId;

    private Long brandId;

    private BigDecimal originPrice;

    private BigDecimal price;

    private BigDecimal originalPrice;
    private String subTitle;

    private String corner;

    /**
     * 展示销量（= realSales）
     */
    private Integer sales;

    /**
     * 真实销量（已支付订单统计）
     */
    private Integer realSales;

    /**
     * 虚拟销量（已停用，固定为 0）
     */
    private Integer virtualSales;

    private String picUrl;

    private String[] album;

    private String unit;

    private String description;

    private String detail;

    private String spuType;

    private String spuTypeDesc;

    private Integer status;

    /**
     * SPU总库存（所有SKU库存之和）
     */
    private Integer totalStock;

}
