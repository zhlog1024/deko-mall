package com.deko.mall.ums.model.form;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class MemberSupplierAuditAddParam implements Serializable {
    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "供应商名称(公司名/别名)")
    private String supplierName;

    @ApiModelProperty(value = "供应商手机号")
    private String supplierMobile;

    @ApiModelProperty(value = "联系人名称")
    private String contactsName;

    @ApiModelProperty(value = "联系人电话")
    private String contactsMobile;

    @ApiModelProperty(value = "联系地址-跨境商家专有")
    private String contactsAddress;

    @ApiModelProperty(value = "客服热线")
    private String contactsCustomerHotline;

    @ApiModelProperty(value = "企业全称(供应商注册公司名称)")
    private String companyName;

    @ApiModelProperty(value = "企业logo")
    private String companyLogo;

    @ApiModelProperty(value = "企业地址省id")
    private Long companyProvinceId;

    @ApiModelProperty(value = "企业地址市id")
    private Long companyCityId;

    @ApiModelProperty(value = "企业地址区id")
    private Long companyCountyId;

    @ApiModelProperty(value = "企业地址镇id")
    private Long companyTownId;

    @ApiModelProperty(value = "国内电商：企业地址-详细地址")
    private String companyAddress;

    @ApiModelProperty(value = "企业营业执照原件")
    private String companyBusinessLicenseImg;

    @ApiModelProperty(value = "企业营业执照到期时间(2099-12-12表示永久)")
    private Date companyBusinessLicenseExpireTime;


    @ApiModelProperty(value = "企业统一信用代码")
    private String companyUnifiedCreditCode;


    @ApiModelProperty(value = "法人姓名")
    private String legalPersonName;

    @ApiModelProperty(value = "法人身份证号")
    private String legalPersonIdentity;

    @ApiModelProperty(value = "法人身份证原件(第一张正面 第二张反面,逗号隔开)")
    private String legalPersonIdentityImgs;

    @ApiModelProperty(value = "法人身份证到期时间(2099-12-12表示永久)")
    private Date legalPersonIdentityExpireTime;


    @ApiModelProperty(value = "企业对公账号")
    private String bankAccountNo;

    @ApiModelProperty(value = "开户银行名称")
    private String bankAccountName;

    @ApiModelProperty(value = "存款账户信息")
    private String bankMainAccountImg;

    @ApiModelProperty(value = "开户行所属支行")
    private String bankSubbranch;

    @ApiModelProperty(value = "开户行所在的省id")
    private Long bankDepositProvinceId;

    @ApiModelProperty(value = "开户行所在市id")
    private Long bankDepositCityId;

    @ApiModelProperty(value = "委托授权书")
    private String powerOfAttorneyFile;

}