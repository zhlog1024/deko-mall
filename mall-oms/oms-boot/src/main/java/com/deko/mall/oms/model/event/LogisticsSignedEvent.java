package com.deko.mall.oms.model.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 物流已签收事件
 * <p>
 * 由 LogisticsInfoServiceImpl 在检测到物流签收时发布，
 * 由 LogisticsSignedListener 监听并触发订单自动确认收货。
 */
@Getter
public class LogisticsSignedEvent extends ApplicationEvent {

    /** 关联的单据编号（发货物流时为订单号，退货物流时为售后单号） */
    private final String bizNo;
    /** 物流单号 */
    private final String trackingNo;
    /** 物流类型：1-发货 2-退货 */
    private final Integer logisticsType;

    public LogisticsSignedEvent(Object source, String bizNo, String trackingNo, Integer logisticsType) {
        super(source);
        this.bizNo = bizNo;
        this.trackingNo = trackingNo;
        this.logisticsType = logisticsType;
    }
}
