package com.deko.mall.pms.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 供应商视图对象
 */
@Data
@Schema(description = "供应商视图对象")
public class SupplierVO {

    @Schema(description = "供应商ID")
    private Long id;

    @Schema(description = "登录账号")
    private String loginName;

    @Schema(description = "供应商编码")
    private String supplierCode;

    @Schema(description = "供应商名称")
    private String supplierName;


    @Schema(description = "营业执照号")
    private String businessLicense;

    @Schema(description = "审核状态(0-待审核,1-审核通过,2-审核拒绝)")
    private Integer auditStatus;

    @Schema(description = "审核状态名称")
    private String auditStatusLabel;

    @Schema(description = "审核备注")
    private String auditRemark;

    @Schema(description = "退货人")
    private String returnContact;

    @Schema(description = "退货电话")
    private String returnPhone;

    @Schema(description = "退货地址")
    private String returnAddress;

    @Schema(description = "店铺名称")
    private String shopName;

    @Schema(description = "店铺logo")
    private String shopLogo;

    @Schema(description = "店铺简介")
    private String shopDescription;


    @Schema(description = "可用余额")
    private BigDecimal availableBalance;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
