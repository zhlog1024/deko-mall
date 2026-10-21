package com.deko.mall.pms.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品实体
 */
@Data
@Accessors(chain = true)
public class PmsSpuVO {

    @TableId(type = IdType.AUTO)
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
    private String spuType;
    /**
     * 商品类型描述（普通商品）
     */
    private String spuTypeDesc;
    /**
     * 是否需要发货
     */
    private Boolean needDelivery;
    /**
     * 商品状态
     * 1-上架，2-下架，0-草稿
     */
    private Integer status;
    /**
     * 供应商ID
     */
    private Long supplierId;
    /**
     * 审核状态
     * 0-待审核，1-审核通过，2-拒绝
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
     * 禁售区域ID
     */
    private Long forbiddenAreaId;
    /**
     * 审核人
     */
    private String auditUser;
    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 提交审核时间
     */
    private LocalDateTime submitTime;

    private String categoryName;
    private String brandName;
    private String supplierName;
    private List<PmsSkuVO> skuList;
    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
