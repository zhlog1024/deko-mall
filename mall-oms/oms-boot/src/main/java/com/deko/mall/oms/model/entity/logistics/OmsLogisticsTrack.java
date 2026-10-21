package com.deko.mall.oms.model.entity.logistics;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 物流轨迹信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("oms_logistics_track")
public class OmsLogisticsTrack extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 物流运单ID
     */
    private Long logisticsId;

    /**
     * 物流单号(冗余字段，方便查询)
     */
    private String trackingNo;

    /**
     * 物流状态
     *
     * @see com.deko.mall.oms.enums.LogisticsStatusEnum
     */
    private Integer status;

    /**
     * 物流事件
     */
    private String trackEvent;

    /**
     * 事件发生时间
     */
    private LocalDateTime eventTime;
}
