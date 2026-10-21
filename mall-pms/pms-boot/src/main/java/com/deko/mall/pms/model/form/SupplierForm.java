package com.deko.mall.pms.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 供应商表单对象
 */
@Data
@Schema(description = "供应商表单对象")
public class SupplierForm {

    @Schema(description = "供应商ID")
    private Long id;


    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "登录账号")
    private String loginName;


    @Schema(description = "登录密码")
    private String password;

    @Schema(description = "确认密码")
    private String confirmPassword;


    @NotBlank(message = "供应商名称不能为空")
    @Schema(description = "供应商名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String supplierName;


    @NotBlank(message = "营业执照号不能为空")
    @Schema(description = "营业执照号")
    private String businessLicense;

    @Schema(description = "退货收货人")
    private String returnContact;

    @Schema(description = "退货联系电话")
    private String returnPhone;

    @Schema(description = "退货收货地址")
    private String returnAddress;

    @Schema(description = "店铺名称（默认取供应商名称）")
    private String shopName;

    @Schema(description = "店铺logo（入驻时填写）")
    private String shopLogo;

    @Schema(description = "店铺简介（选填）")
    private String shopDescription;


}
