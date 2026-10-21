package com.deko.mall.oms.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单售后商品明细表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OmsOrderAfterSaleItemVO {

    /**
     * 售后商品ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 售后ID
     */
    private Long afterSaleId;

    /**
     * 订单商品ID
     */
    private Long orderItemId;

    /**
     * 商品ID
     */
    private Long spuId;

    /**
     * 商品SKU ID
     */
    private Long skuId;

    /**
     * 商品SKU编码（下单商品项 sku_sn 快照）
     */
    private String skuSn;

    /**
     * 商品名称
     */
    private String skuName;

    /**
     * 商品图片
     */
    private String picUrl;

    /**
     * 申请数量
     */
    private Integer quantity;

    /**
     * 商品单价(元)
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
     * 退款金额(元)
     */
    private BigDecimal totalAmount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 商品行履约状态(0取消/1待发货/2已发货/3已收货/4售后中/5已退款)
     */
    private Integer status;

    /**
     * 商品行退款状态(0正常/1全额退款/2部分退款)
     */
    private Integer refundStatus;

    /**
     * 商品行退款数量
     */
    private Integer refundQuantity;

    /**
     * 商品行退款金额(元)
     */
    private BigDecimal refundAmount;

}
