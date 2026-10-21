package com.deko.mall.ums.model.form;

import com.deko.mall.ums.enums.AssetOperateSceneEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Schema(description = "资产增减")
@Data
public class OperatorMemberParam {

    @Schema(description = "会员ID")
    private Long memberId;
    @Schema(description = "资产编码")
    private String assetCode;
    @Schema(description = "资产数量")
    @NotNull(message = "资产数量不能为空！")
    private Long num;
    @Schema(description = "订单号")
    private String orderToken;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "操作类型")
    private AssetOperateSceneEnum operationType;
}
