package com.deko.mall.oms.model.entity.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import com.deko.mall.oms.enums.AfterSaleStatusEnum;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单售后表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("oms_order_after_sale")
@EqualsAndHashCode(callSuper = true)
public class OmsOrderAfterSale extends BaseEntity {

    /**
     * 售后ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 售后单号
     */
    private String afterSaleNo;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单商品行ID（一售后单=一商品行，与售后明细一致）
     */
    private Long orderItemId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 会员ID
     */
    private Long memberId;


    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 供应商名称(下单快照)
     */
    private String supplierName;

    /**
     * 店铺ID(下单快照，用户侧按店铺维度展示)
     */
    private Long shopId;

    /**
     * 店铺名称(下单快照)
     */
    private String shopName;

    /**
     * 售后类型(1-仅退款，2-退货退款)
     */
    private Integer afterSaleType;

    /**
     * 申请原因
     */
    private String reason;

    /**
     * 问题描述
     */
    private String description;

    /**
     * 申请凭证图片，多个以逗号分隔
     */
    private String applyPics;

    /**
     * 退货凭证图片，多个以逗号分隔
     */
    private String returnPics;

    /**
     * 退款金额(元)
     */
    private BigDecimal refundAmount;

    /**
     * 使用的积分金额(元)
     */
    private BigDecimal usePointsAmount;


    /**
     * 退款状态
     *
     * @see AfterSaleStatusEnum
     */
    private Integer status;

    /**
     * 处理人
     */
    private String handleBy;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 处理备注
     */
    private String handleNote;


    /**
     * 物流公司编码
     */
    private String carrierCode;

    /**
     * 退货物流公司
     */
    private String carrierName;

    /**
     * 退货物流单号
     */
    private String trackingNo;

    /**
     * 发货人手机号
     */
    private String senderPhone;

    /**
     * 退货申请时间
     */
    private LocalDateTime returnTime;

    /**
     * 收货时间
     */
    private LocalDateTime receiveTime;


    /**
     * 退款申请时间
     */
    private LocalDateTime refundTime;

    /**
     * 退款到账时间
     */
    private LocalDateTime refundSuccessTime;

    /**
     * 退款方式(1->支付宝; 2->微信; 3->银联; 4->原路返回)
     */
    private Integer refundType;

    /**
     * 备注
     */
    private String remark;

    /**
     * 逻辑删除(0->正常; 1->已删除)
     */
    private Integer deleted;

}
