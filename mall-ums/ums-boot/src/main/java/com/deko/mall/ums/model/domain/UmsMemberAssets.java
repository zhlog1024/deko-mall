package com.deko.mall.ums.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@TableName(value = "ums_member_assets")
@Data
public class UmsMemberAssets extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "会员ID")
    private Long memberId;

    /**
     * 资产编码（POINT 积分）
     */
    @Schema(description = "资产编码")
    private String assetCode;

    @Schema(description = "可用数量")
    private BigDecimal availableNum;

    private Integer status;
}