package com.deko.mall.pms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 运费计算参数
 */
@Data
@Schema(description = "运费计算参数")
public class FreightCalcParam {

    @Schema(description = "运费模板ID")
    private Long templateId;

    @Schema(description = "省份编码")
    private String provinceCode;

    @Schema(description = "城市编码")
    private String cityCode;

    @Schema(description = "区县编码")
    private String districtCode;

    @Schema(description = "商品总数量")
    private Integer totalQuantity;

    @Schema(description = "商品总金额(单位：元)")
    private BigDecimal totalAmount;

    @Schema(description = "商品总重量(单位：克)")
    private Integer totalWeight;
}
