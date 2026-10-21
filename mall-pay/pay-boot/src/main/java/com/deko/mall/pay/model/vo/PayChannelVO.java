package com.deko.mall.pay.model.vo;

import lombok.Data;

import java.util.Map;

/**
 * 支付渠道展示对象
 */
@Data
public class PayChannelVO {

    /**
     * 渠道编码
     */
    private String channelCode;

    /**
     * 渠道名称
     */
    private String channelName;

    /**
     * 渠道图标
     */
    private String channelIcon;

    /**
     * 适用设备类型（H5 / MINI / APP / ALL）
     */
    private String deviceType;

    /**
     * 是否可用（后端按业务规则判断），用于前端灰显/提示
     */
    private Boolean enabled;

    /**
     * 扩展元信息（非敏感），比如限额、说明等
     */
    private Map<String, Object> meta;
}
