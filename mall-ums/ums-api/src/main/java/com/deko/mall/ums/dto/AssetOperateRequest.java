package com.deko.mall.ums.dto;

import com.deko.mall.ums.enums.AssetOperateSceneEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 资产操作请求DTO
 */
@Data
@Schema(description = "资产操作请求")
public class AssetOperateRequest {

    @NotNull(message = "会员ID不能为空")
    @Schema(description = "会员ID")
    private Long memberId;

    @NotNull(message = "资产类型不能为空")
    @Schema(description = "资产类型：POINT-积分")
    private String assetCode;

    @NotNull(message = "操作数量不能为空")
    @Schema(description = "操作数量（正数）")
    private BigDecimal num;

    @NotNull(message = "操作类型不能为空")
    @Schema(description = "操作类型")
    private AssetOperateSceneEnum operateType;

    @Schema(description = "关联业务单号（订单号/退款单号等）")
    private String bizNo;

    @Schema(description = "备注")
    private String remark;

    // 转账专用字段
    @Schema(description = "转入会员ID（转账场景）")
    private Long toMemberId;

    @Schema(description = "转入会员手机号（转账场景）")
    private String toMemberMobile;

    // 内部调整专用字段
    @Schema(description = "调整原因")
    private String adjustReason;
}
