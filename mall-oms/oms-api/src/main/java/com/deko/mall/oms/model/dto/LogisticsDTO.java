package com.deko.mall.oms.model.dto;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 支付回调结果
 */
@Data
@Builder
@Accessors(chain = true)
public class LogisticsDTO {

    /**
     * 单据编号
     * - 发货物流：订单编号(orderNo)
     * - 退货物流：售后单编号(refundNo)
     */
    private String bizNo;

    /**
     * 物流单号
     */
    private String trackingNo;

    /**
     * 物流公司编码
     */
    private String carrierCode;

    /**
     * 收件人电话
     */
    private String phone;

    /**
     * 商品名称
     */
    private String spuName;

    /**
     * 微信用户openid (可选，用于推送物流消息)
     */
    private String openid;

    /**
     * 支付单号 (可选，用于关联支付信息)
     */
    private String paymentChannelNo;

    /**
     * 支付渠道编码，用于策略判断（如 WECHAT_JSAPI 需要上报物流）
     */
    private String payChannel;

    /**
     * 渠道交易号（微信 transaction_id），用于微信发货报备 order_number_type=2
     */
    private String channelTradeNo;

    /**
     * 物流类型
     *
     * @see com.deko.mall.oms.enums.LogisticsTypeEnum
     */
    private Integer logisticsType;

    /**
     * 发货类型：1线上物流 2线下发货
     */
    private Integer deliveryType;
}
