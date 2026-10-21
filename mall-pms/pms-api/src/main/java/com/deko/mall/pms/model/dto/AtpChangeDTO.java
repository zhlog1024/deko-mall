package com.deko.mall.pms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 供应商可承诺量（ATP）变动明细。
 */
@Schema(description = "ATP 变动明细")
@Data
public class AtpChangeDTO implements Serializable {

    @Schema(description = "SKU ID")
    private Long skuId;

    @Schema(description = "SPU ID（成交扣减时用于累加销量，其它场景可空）")
    private Long spuId;

    @Schema(description = "数量（正数）")
    private Integer quantity;
}
