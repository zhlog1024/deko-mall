package com.deko.mall.ums.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@TableName(value = "ums_member_assets_flow")
@Data
public class UmsMemberAssetsFlow extends BaseEntity {
    @TableId(type = IdType.AUTO)
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

    @Schema(description = "变更前可用金额")
    private BigDecimal beforeAvailableNum;

    @Schema(description = "变更后可用金额")
    private BigDecimal afterAvailableNum;

    @Schema(description = "操作类型（FREEZE-冻结/UNFREEZE-解冻/USE-使用/GRANT-发放/DEDUCT-扣减）")
    private String type;

    @Schema(description = "交易场景编码")
    private String sceneCode;

    @Schema(description = "交易场景描述")
    private String sceneDescription;

    @Schema(description = "关联订单号")
    private String reqNo;

    @Schema(description = "备注")
    private String remark;
}