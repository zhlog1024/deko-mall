package com.deko.mall.pms.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PmsSpuAttributeForm {

    private String id;

    @Schema(description = "商品规格属性id")
    private Long attributeId;
    @Schema(description = "商品规格属性名称")
    private String name;

    @Schema(description = "商品规格属性值")
    private String value;

    private String picUrl;

}
