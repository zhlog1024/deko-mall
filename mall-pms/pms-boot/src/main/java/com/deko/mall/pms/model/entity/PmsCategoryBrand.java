package com.deko.mall.pms.model.entity;

import com.deko.mall.common.base.BaseEntity;
import lombok.Data;

/**
 * 分类品牌
 */
@Data
public class PmsCategoryBrand extends BaseEntity {
    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 品牌ID
     */
    private Long brandId;

}
