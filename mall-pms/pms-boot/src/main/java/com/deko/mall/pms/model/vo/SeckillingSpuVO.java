package com.deko.mall.pms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 【应用端】秒杀商品视图对象
 */
@Schema(description = "商品分页对象")
@Data
public class SeckillingSpuVO {

    @Schema(description = "商品ID")
    private Long id;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "商品价格(单位：元)")
    private java.math.BigDecimal price;

    @Schema(description = "图片地址")
    private String picUrl;

}
