package com.deko.mall.oms.model.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 运营后台订单导出VO
 */
@Data
public class OmsOrderExportVO {

    @ExcelProperty("订单编号")
    private String orderNo;

    @ExcelProperty("订单类型")
    private String orderTypeLabel;

    @ExcelProperty("订单总额(元)")
    private BigDecimal totalAmount;

    @ExcelProperty("运费金额(元)")
    private BigDecimal freightAmount;

    @ExcelProperty("订单状态")
    private String statusDesc;

    @ExcelProperty("支付方式")
    private String payChannel;

    @ExcelProperty("下单时间")
    private String createTime;

    @ExcelProperty("支付时间")
    private String paymentTime;

    @ExcelProperty("发货时间")
    private String deliveryTime;

    @ExcelProperty("完成时间")
    private String receiveTime;

    @ExcelProperty("供应商ID")
    private String supplierId;

    @ExcelProperty("商品行ID")
    private Long orderItemId;

    @ExcelProperty("商品ID")
    private Long spuId;

    @ExcelProperty("规格ID")
    private Long skuId;

    @ExcelProperty("供货价(元)")
    private BigDecimal supplyPrice;

    @ExcelProperty("零售价(元)")
    private BigDecimal retailPrice;

    @ExcelProperty("商品规格名称")
    private String skuName;

    @ExcelProperty("商品数量")
    private Integer quantity;

    @ExcelProperty("商品单价(元)")
    private BigDecimal price;

    @ExcelProperty("订单商品总额(元)")
    private BigDecimal orderItemTotalAmount;

    @ExcelProperty("订单备注")
    private String remark;

    @ExcelProperty("收件人姓名")
    private String receiverName;

    @ExcelProperty("收件人电话")
    private String receiverPhone;

    @ExcelProperty("收件人省份")
    private String receiverProvince;

    @ExcelProperty("收件人城市")
    private String receiverCity;

    @ExcelProperty("收件人区县")
    private String receiverDistrict;

    @ExcelProperty("收件人完整地址")
    private String receiverCompleteAddress;

    @ExcelProperty("下单人手机号")
    private String buyerPhone;

    @ExcelProperty("物流公司")
    private String carrierName;

    @ExcelProperty("物流单号")
    private String trackingNo;

    @ExcelProperty("支付申请号")
    private String paymentChannelNo;

    @ExcelProperty("渠道交易号")
    private String channelTradeNo;

    @ExcelProperty("积分抵扣金额(元)")
    private BigDecimal pointsDeductionAmount;

}
