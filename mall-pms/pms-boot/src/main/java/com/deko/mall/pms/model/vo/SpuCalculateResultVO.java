package com.deko.mall.pms.model.vo;

import lombok.Data;
import java.util.List;

/**
 * SPU定价计算结果
 */
@Data
public class SpuCalculateResultVO {
    /**
     * 计算后的SKU列表（含零售毛利率）
     */
    private List<PmsSkuVO> skuList;
    /**
     * 建议的商品类型（NORMAL）
     */
    private String suggestedSpuType;
    /**
     * 商品类型说明
     */
    private String spuTypeDesc;
}
