package com.deko.mall.oms.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 退货物流信息表单
 */
@Data
@Schema(description = "退货物流信息表单")
public class ReturnLogisticsForm {

    @NotNull(message = "售后单id不能为空")
    @Schema(description = "售后单id")
    private Long afterSaleId;

    @NotBlank(message = "物流公司名称不能为空")
    @Schema(description = "物流公司名称")
    private String carrierName;

    @NotBlank(message = "发货人手机号不能为空")
    @Schema(description = "发货人手机号")
    private String senderPhone;


    @NotBlank(message = "物流公司代码不能为空")
    @Schema(description = "物流公司代码")
    private String carrierCode;

    @NotBlank(message = "物流单号不能为空")
    @Schema(description = "物流单号/运单号")
    private String trackingNo;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "退货凭证图片，多个图片用逗号分隔")
    private String returnPics;
}
