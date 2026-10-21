package com.deko.mall.ums.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


import java.math.BigDecimal;


@Schema(description = "积分转账表单对象")
@Data
public class PointsTransferForm {

    @Schema(description = "接收方手机号")
    @NotBlank(message = "接收方手机号不能为空")
    private String receiverMobile;


    @Schema(description = "转账积分数量")
    @NotNull(message = "转账积分数量不能为空")
    @Min(value = 1, message = "转账积分数量必须大于0")
    private BigDecimal num;

    @Schema(description = "转账备注")
    private String remark;
}
