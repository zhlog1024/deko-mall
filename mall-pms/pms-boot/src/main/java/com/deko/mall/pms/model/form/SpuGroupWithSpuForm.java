package com.deko.mall.pms.model.form;


import com.deko.mall.pms.model.entity.PmsSpuGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "商品分组及关联商品表单")
@Data
public class SpuGroupWithSpuForm {
    @Schema(description = "商品分组信息")
    private PmsSpuGroup spuGroup;

    @Schema(description = "关联的商品列表（包含排序信息）")
    private List<SpuSortItem> spuList;
}

