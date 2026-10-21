package com.deko.mall.oms.model.entity.order;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import com.deko.mall.oms.enums.ReceiveAddressTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * 订单收货表
 */
@Builder
@ApiModel("订单收货表")
@Data
@TableName("oms_order_address")
public class OmsOrderAddress extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 收货地址id
     */
    @ApiModelProperty(value = "收货地址id")
    private Long receiveAddressId;
    /**
     * MERCHANT_RECEIVE:商家收货(用于售后)，USER_RECEIVE:用户收货
     */
    @ApiModelProperty(value = "MERCHANT_RECEIVE:商家收货(用于售后)，USER_RECEIVE:用户收货")
    private ReceiveAddressTypeEnum receiveAddressType;
    /**
     * 聚合订单号
     */
    @ApiModelProperty(value = "订单号")
    private String orderNo;
    /**
     * 聚合订单id
     */
    @ApiModelProperty(value = "订单id")
    private Long orderId;
    /**
     * 订单商品id：用于售后，商家发货时无效
     */
    @ApiModelProperty(value = "订单商品id：用于售后，商家发货时无效")
    private Long orderItemId;
    /**
     * 收货地址用户id
     */
    @ApiModelProperty(value = "收货地址用户id")
    private Long userId;
    /**
     * 收货地址用户名字
     */
    @ApiModelProperty(value = "收货地址用户名字")
    private String userName;
    /**
     * 收货地址用户手机号
     */
    @ApiModelProperty(value = "收货地址用户手机号")
    private String userPhone;
    /**
     * 邮编
     */
    @ApiModelProperty(value = "邮编")
    private String zipCode;
    /**
     * 省
     */
    @ApiModelProperty(value = "省")
    private Integer provinceId;
    /**
     * 市
     */
    @ApiModelProperty(value = "市")
    private Integer cityId;
    /**
     * 区
     */
    @ApiModelProperty(value = "区")
    private Integer areaId;
    /**
     * 字段：new_health_order_receive_address.province_name
     * 描述：省份
     */
    private String provinceName;

    /**
     * 字段：new_health_order_receive_address.city_name
     * 描述：市
     */
    private String cityName;

    /**
     * 字段：new_health_order_receive_address.area_name
     * 描述：区
     */
    private String areaName;

    /**
     * 地址
     */
    @ApiModelProperty(value = "地址")
    private String address;
    /**
     * 完整地址
     */
    @ApiModelProperty(value = "完整地址")
    private String completeAddress;
    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;

    private Integer deleted;
}
