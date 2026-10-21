package com.deko.mall.pms.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 【管理端】商品分页视图对象
 */
@Data
@Accessors(chain = true)
public class PmsSpuPageVO {

    private Long id;
    private String name;
    private Long categoryId;
    private Long brandId;
    private BigDecimal originPrice;

    private BigDecimal price;

    private Integer sales;
    private String picUrl;
    private String[] album;

    private String unit;
    private String description;
    private String detail;
    private Integer status;
    /**
     * 是否需要发货
     */
    private Boolean needDelivery;
    private Integer auditStatus;
    private Long freightTemplateId;
    private Long forbiddenAreaId;
    private Long supplierId;
    private String supplierName;
    private String remark;
    private LocalDateTime submitTime;

    private LocalDateTime auditTime;

    private String auditUser;
    private String categoryName;
    private String brandName;
    private List<PmsSkuVO> skuList;
    /**
     * 商品类型
     */
    private String spuType;
    /**
     * 商品类型描述
     */
    private String spuTypeDesc;
}
