package com.deko.mall.oms.model.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.deko.mall.oms.util.LocalDateTimeConverter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 售后单导出数据模型
 */
@Data
@Schema(description = "售后单导出数据")
public class AfterSaleExportVO {

    @ExcelProperty("售后单号")
    private String afterSaleNo;

    @ExcelProperty("订单编号")
    private String orderNo;

    @ExcelProperty("会员ID")
    private Long memberId;

    @ExcelProperty("售后状态")
    private String statusLabel;

    @ExcelProperty("退款金额")
    private BigDecimal refundAmount;

    @ExcelProperty("申请原因")
    private String reason;

    @ExcelProperty("申请说明")
    private String description;

    @ExcelProperty("物流公司")
    private String carrierName;

    @ExcelProperty("物流单号")
    private String trackingNo;

    @ExcelProperty(value = "申请时间", converter = LocalDateTimeConverter.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @ExcelProperty(value = "审核时间", converter = LocalDateTimeConverter.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime handleTime;

    @ExcelProperty("审核备注")
    private String handleNote;

    @ExcelProperty(value = "退款时间", converter = LocalDateTimeConverter.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime refundTime;

    @ExcelProperty("处理人")
    private String handler;

    @ExcelProperty("商户备注")
    private String merchantNote;
}
