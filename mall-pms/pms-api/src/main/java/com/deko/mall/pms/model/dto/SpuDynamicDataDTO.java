package com.deko.mall.pms.model.dto;

import lombok.Data;

/**
 * 商品动态数据传输对象
 * 用于页面装修系统中展示商品的动态数据
 * 包含SPU的销量、库存、最低价和上下架状态
 */
@Data
public class SpuDynamicDataDTO {

    /**
     * SPU ID
     */
    private Long spuId;


    /**
     * 商品名称
     */
    private String spuName;

    /**
     * SPU展示销量（= real_sales）
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

    /**
     * SPU总库存（所有SKU库存之和）
     */
    private Integer totalStock;

    /**
     * SPU最低销售价
     */
    private Long minPrice;

    /**
     * 上下架状态 (1-上架, 0-下架)
     */
    private Integer publishStatus;
}
