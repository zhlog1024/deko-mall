package com.deko.mall.oms.model.entity.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 订单商品明细
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OmsOrderItem extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 商品SPU_ID
     */
    private Long spuId;

    /**
     * 商品名称
     */
    private String spuName;

    /**
     * 规格ID
     */
    private Long skuId;

    /**
     * SKU 编号
     */
    private String skuSn;

    /**
     * 规格名称
     */
    private String skuName;

    /**
     * 商品sku图片
     */
    private String picUrl;

    /**
     * 商品单价(单位：元)
     */
    private BigDecimal price;
    /**
     * 零售价(元)
     */
    private BigDecimal retailPrice;
    /**
     * 供货价(元)
     */
    private BigDecimal supplyPrice;
    /**
     * 商品数量
     */
    private Integer quantity;

    /**
     * 商品总金额(单位：元)
     */
    private BigDecimal totalAmount;

    /**
     * 逻辑删除(0:正常；1:已删除)
     */
    private Integer deleted;

    /**
     * 运费模板ID
     */
    private Long freightTemplateId;

    /**
     * 运费金额(单位：元)
     */
    private BigDecimal freightAmount;

    /**
     * 禁售区域ID
     */
    private Long forbiddenAreaId;

    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 供应商名称(下单快照)
     */
    private String supplierName;

    /**
     * 店铺ID(下单快照)
     */
    private Long shopId;

    /**
     * 店铺名称(下单快照)
     */
    private String shopName;

    /**
     * 售后状态（0：正常，1：全额退款，2：部分退款）
     */
    private Integer refundStatus;

    /**
     * 退款数量
     */
    private Integer refundQuantity;

    /**
     * 退款金额(单位：元)
     */
    private BigDecimal refundAmount;

    /**
     * 是否已评论(0-否, 1-是)
     */
    private Integer isCommented;

    /**
     * 履约状态(0-已取消;1-待发货;2-已发货;3-已收货;4-售后中;5-已退款)
     * 商品项为履约/售后承载单元，状态机独立于订单流转
     */
    private Integer status;

    /**
     * 优惠券抵扣分摊(按行金额占比,尾差最后行兜底)
     */
    private BigDecimal couponDeductionAmount;

    /**
     * 积分抵扣分摊
     */
    private BigDecimal pointsDeductionAmount;

    /**
     * 行实付 = 行商品额+行运费-Σ行抵扣（快照；历史数据直拷订单级实付）
     */
    private BigDecimal paymentAmount;

}
