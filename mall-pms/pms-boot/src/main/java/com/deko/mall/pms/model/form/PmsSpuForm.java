package com.deko.mall.pms.model.form;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品SPU表单对象
 */
@Data
public class PmsSpuForm {

    private Long id;
    private String name;
    private Long categoryId;
    private Long brandId;
    /**
     * 商品类型
     */
    private String spuType;
    private BigDecimal originPrice;
    private BigDecimal price;
    private String picUrl;
    private String[] subPicUrls;
    private String description;
    private String detail;
    /**
     * 供应商ID
     */
    private Long supplierId;
    /**
     * 店铺ID(冗余，商品挂店铺)
     */
    private Long shopId;
    /**
     * 商品状态(ON_SALE-上架，OFF_SALE-下架，DRAFT-草稿)
     */
    private String status;
    /**
     * 审核状态(0-待审核，1-审核通过，2-拒绝)
     */
    private Integer auditStatus;
    /**
     * 拒绝原因
     */
    private String remark;
    /**
     * 运费模板ID
     */
    private Long freightTemplateId;
    /**
     * 是否需要发货
     */
    private Boolean needDelivery;
    /**
     * 禁售区域ID
     */
    private Long forbiddenAreaId;
    private List<PmsSpuAttributeForm> attrList;
    private List<PmsSpuAttributeForm> specList;
    private List<PmsSkuForm> skuList;
}
