package com.deko.mall.oms.model.entity.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import lombok.*;

import java.math.BigDecimal;

/**
 * 订单售后商品明细表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("oms_order_after_sale_item")
@EqualsAndHashCode(callSuper = true)
public class OmsOrderAfterSaleItem extends BaseEntity {

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
     * 售后申请时商品项状态快照(1待发货/2已发货/3已收货; 售后拒绝/取消/部分退款时恢复用)
     */
    private Integer preStatus;

}
