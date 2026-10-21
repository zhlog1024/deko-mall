package com.deko.mall.pay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 支付配置实体
 */
@Data
@TableName(value = "pay_config", autoResultMap = true)
@EqualsAndHashCode(callSuper = true)
public class PayConfig extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 支付渠道编码（PayChannelEnum） */
    private String payChannel;

    /** 应用ID */
    private String appId;

    /** 商户ID */
    private String mchId;

    /** 支付回调地址 */
    private String paymentNotifyUrl;

    /** 退款回调地址 */
    private String refundNotifyUrl;

    /** 是否启用 1-是 0-否 */
    private Boolean enabled;

    /** 适用设备类型，逗号分隔：H5,MINI,APP */
    private String deviceType;

    /** admin 识别名称（含聚合商+方式） */
    private String channelName;

    /** C端收银台展示名称 */
    private String cashierName;

    /** 渠道图标URL */
    private String channelIcon;

    /** 允许的用户ID集合（逗号分隔），为空表示对所有用户开放 */
    private String allowedUserIds;

    /** 扩展配置JSON */
    private String extConfig;

    /** 解析后的扩展配置对象 */
    @TableField(exist = false)
    private transient PayExtConfig parsedExtConfig;
    /** 逻辑删除 0=正常 1=删除 */
    @TableLogic
    private Integer deleted;

}
