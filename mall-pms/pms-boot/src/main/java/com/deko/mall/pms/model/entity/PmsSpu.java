package com.deko.mall.pms.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class PmsSpu extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long categoryId;
    private Long brandId;
    /**
     * 展示销量（= realSales）
     */
    private Integer sales;
    /**
     * 真实销量（已支付订单统计）
     */
    private Integer realSales;
    /**
     * 虚拟销量（已停用，固定为 0，保留列兼容）
     */
    private Integer virtualSales;
    private String picUrl;
    private String[] album;

    private String unit;
    private String description;
    private String detail;
    private String spuType;
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
     * 店铺ID(冗余，商品挂店铺，用户侧统一按店铺过滤)
     */
    private Long shopId;
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

    @TableField(exist = false)
    private String categoryName;
    @TableField(exist = false)
    private String brandName;
    @TableField(exist = false)
    private String supplierName;
    @TableField(exist = false)
    private List<PmsSku> skuList;
}
