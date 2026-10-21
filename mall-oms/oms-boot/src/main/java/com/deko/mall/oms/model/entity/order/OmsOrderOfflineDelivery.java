package com.deko.mall.oms.model.entity.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 线下发货申请表
 */
@Data
@Builder
@Accessors(chain = true)
@TableName("oms_order_offline_delivery")
public class OmsOrderOfflineDelivery extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 线下发货凭证URLs，逗号分隔
     */
    private String offlineProof;

    /**
     * 备注/说明（如：同城配送、自提）
     */
    private String offlineRemark;

    /**
     * 物流公司编码（可选）
     */
    private String carrierCode;

    /**
     * 物流公司名称（可选）
     */
    private String carrierName;

    /**
     * 物流单号（可选）
     */
    private String trackingNo;

    /**
     * 申请时间
     */
    private LocalDateTime applyTime;

    /**
     * 审核状态：1通过（默认自动通过）
     */
    private Integer auditStatus;

    /**
     * 审核人（系统自动审核填SYSTEM）
     */
    private String auditor;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 审核备注（默认：系统自动审核）
     */
    private String auditRemark;
}
