package com.deko.mall.ums.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UmsMemberAssetsFlowVO {
    private Long id;

    private Long assetId;
    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "转出会员ID（资产流出方）")
    private Long fromMemberId;

    @Schema(description = "接收会员ID（资产接收方）")
    private Long toMemberId;

    @Schema(description = "变动数量")
    private BigDecimal num;

    @Schema(description = "变动类型")
    private String type;

    @Schema(description = "交易场景编码")
    private String sceneCode;

    @Schema(description = "交易场景描述")
    private String sceneDescription;

    @Schema(description = "关联订单号")
    private String reqNo;

    @Schema(description = "备注")
    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
