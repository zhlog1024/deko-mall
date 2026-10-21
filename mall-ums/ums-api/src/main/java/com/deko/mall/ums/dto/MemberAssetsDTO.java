package com.deko.mall.ums.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 会员积分信息DTO
 */
@Data
public class MemberAssetsDTO {

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "会员资产ID")
    private Long id;

    @Schema(description = "资产编码")
    private String assetCode;
    @Schema(description = "可用数量")
    private BigDecimal availableNum;

    @Schema(description = "状态")
    private Integer status;
}
