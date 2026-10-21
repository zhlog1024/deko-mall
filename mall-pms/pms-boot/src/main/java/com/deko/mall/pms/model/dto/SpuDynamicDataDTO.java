package com.deko.mall.pms.model.dto;

import lombok.Data;

/**
 * SPU动态数据DTO
 * <p>
 * 用于保存SPU维度的动态数据，包括：
 * - 最低售价：SPU关联的所有SKU中的最低价格
 * - 总库存：SPU关联的所有SKU的库存总和
 * - 销量：SPU的销售量
 * - 上架状态：SPU的上架状态
 */
@Data
public class SpuDynamicDataDTO {

    /**
     * 商品SPU ID
     */
    private Long spuId;

    /**
     * 商品最低售价（元），从SPU关联的所有SKU中获取最低价
     */
    private Long minPrice;

    /**
     * 商品总库存，累加SPU关联的所有SKU的库存
     */
    private Integer totalStock;

    /**
     * 商品展示销量（= real_sales），直接从SPU表获取
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
     * 商品上架状态：0-下架，1-上架
     */
    private Integer publishStatus;
}
