package com.deko.mall.ums.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Schema(description = "积分转账表单对象")
@Data
public class AssetTransferForm {

    @Schema(description = "接收方手机号")
    @NotBlank(message = "接收方手机号不能为空")
    private String receiverMobile;

    @Schema(description = "转账数量")
    @NotNull(message = "转账数量不能为空")
    @Min(value = 1, message = "转账数量必须大于0")
    private Long points;

    @Schema(description = "转账备注")
    private String remark;
}
