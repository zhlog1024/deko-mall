package com.deko.mall.pms.model.form;

import lombok.Data;
import java.util.List;

/**
 * SPU定价计算请求表单
 */
@Data
public class SpuPricingCalculateForm {
    /**
     * SPU ID（编辑时传入）
     */
    private Long spuId;
    /**
     * SKU列表（包含零售价、供货价）
     */
    private List<PmsSkuForm> skuList;
    /**
     * 用户手动选择的商品类型（运营后台切换类型时传入，后端据此强制执行对应定价规则）
     */
    private String spuType;
}
