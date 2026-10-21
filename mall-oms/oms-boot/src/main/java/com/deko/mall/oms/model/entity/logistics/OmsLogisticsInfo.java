package com.deko.mall.oms.model.entity.logistics;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物流运单表
 */
@Data
@TableName("oms_logistics_info")
public class OmsLogisticsInfo extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单号
     */
    private String bizNo;

    /**
     * 物流类型：1-发货物流，2-退货物流
     *
     * @see com.deko.mall.oms.enums.LogisticsTypeEnum
     */
    private Integer logisticsType;

    /**
     * 物流单号
     */
    private String trackingNo;

    /**
     * 物流公司编码
     */
    private String carrierCode;
    /**
     * 物流公司名称
     */
    private String carrierName;

    /**
     * 收件人电话
     */
    private String phone;

    /**
     * 物流状态
     */
    private Integer status;

    /**
     * 最后查询时间
     */
    private LocalDateTime lastQueryTime;

    /**
     * 补偿查询次数
     */
    private Long queryCount = 0L;

    /**
     * 最后回调时间
     */
    private LocalDateTime lastCallbackTime;

    /**
     * 备注
     */
    private String remark;


}
