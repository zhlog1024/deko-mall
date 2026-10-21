package com.deko.mall.ums.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 供应商信息审核
 */
@Schema(description = "填写公司信息-请求入参")
@Data
public class MemberSupplierAuditAddReq implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "企业全称(供应商注册公司名称)", required = true)
    @NotBlank(message = "企业全称(供应商注册公司名称)不能为空！")
    private String companyName;

    /*@Schema(description = "供应商账号(供应商注册时的手机号)", required = true)
    @NotBlank(message = "供应商手机号不能为空！")
    @Pattern(regexp = "^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$", message = "请填写正确的手机号")
    private String supplierMobile;*/
    @Schema(description = "供应商名称(公司名/别名)")
    private String supplierName;


    @Schema(description = "企业营业执照原件", required = true)
    @NotBlank(message = "企业营业执照原件不能为空！")
    private String companyBusinessLicenseImg;
    @Schema(description = "企业logo")
    private String companyLogo;

    @Schema(description = "企业地址省id")
    private Long companyProvinceId;

    @Schema(description = "企业地址市id")
    private Long companyCityId;

    @Schema(description = "企业地址区id")
    private Long companyCountyId;

    @Schema(description = "企业地址镇id")
    private Long companyTownId;

    @Schema(description = "国内电商：企业地址-详细地址")
    private String companyAddress;

    @Schema(description = "企业统一信用代码", required = true)
//    @NotBlank(message = "企业统一信用代码不能为空！")
//    @Pattern(regexp = "^\\w{15}(\\w{3})*$", message = "统一信用代码由15或18位的数字和字母组成")
    private String companyUnifiedCreditCode;

    @DateTimeFormat
    @Schema(description = "企业营业执照到期时间")
//    @NotBlank(message = "企业营业执照到期时间不能为空！")
    private Date companyBusinessLicenseExpireTime;

    @Schema(description = "企业营业执照到期是否长期 0否1是")
    private Boolean companyBusinessLicenseLongTerm = false;


    @Schema(description = "法人姓名", required = true)
    @NotBlank(message = "法人姓名不能为空！")
    private String legalPersonName;

    @Schema(description = "法人身份证号", required = true)
    @NotBlank(message = "法人身份证号不能为空！")
    @Pattern(regexp = "^\\d{17}[\\d|Xx]|\\d{15}$", message = "身份证号由15或18位组成")
    private String legalPersonIdentity;

    @Schema(description = "法人身份证原件(第一张正面 第二张反面)", required = true)
    @NotNull(message = "法人身份证原件")
    private String legalPersonIdentityImgs;

    @Schema(description = "法人身份证到期时间(2099-12-12表示永久)")
//    @NotNull(message = "法人身份证到期时间不能为空")
    private Date legalPersonIdentityExpireTime;

    @Schema(description = "身份证有效期是否长期 0否1是")
    private Boolean legalPersonIdentityLongTerm = false;

    @Schema(description = "企业对公账号", required = true)
    private String bankAccountNo;

    @Schema(description = "开户银行名称", required = true)
    private String bankAccountName;

    @Schema(description = "存款账户信息", required = true)
    private String bankMainAccountImg;

    @Schema(description = "开户行所属支行", required = true)
    private String bankSubbranch;

    @Schema(description = "开户行所在的省id", required = true)
    private Long bankDepositProvinceId;

    @Schema(description = "开户行所在市id", required = true)
    private Long bankDepositCityId;


    @Schema(description = "联系人名称", required = true)
    @NotBlank(message = "联系人名称不能为空！")
    private String contactsName;

    @Schema(description = "联系人电话", required = true)
    @NotBlank(message = "联系人电话不能为空！")
    @Size(min = 6, max = 12, message = "座机可以试试6-12位, 也可以是手机号的11位")
    private String contactsMobile;

    @Schema(description = "客服热线", required = true)
    private String contactsCustomerHotline;

    @Schema(description = "委托授权书", required = true)
    private String powerOfAttorneyFile;
}