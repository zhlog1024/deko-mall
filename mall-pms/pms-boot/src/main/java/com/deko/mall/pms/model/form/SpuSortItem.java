package com.deko.mall.pms.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "商品排序项")
@Data
public class SpuSortItem {
    @Schema(description = "商品SPU ID")
    private Long spuId;

    @Schema(description = "排序值（升序）")
    private Integer sort;
}

