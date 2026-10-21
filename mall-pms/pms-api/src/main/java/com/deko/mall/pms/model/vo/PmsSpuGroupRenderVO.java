package com.deko.mall.pms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 商品分组渲染VO
 */
@Data
@Schema(description = "商品分组渲染VO")
public class PmsSpuGroupRenderVO {

    @Schema(description = "分组ID")
    private Long id;

    @Schema(description = "分组名称")
    private String name;

    @Schema(description = "分组描述")
    private String description;

    @Schema(description = "展示顺序")
    private Integer sort;

    @Schema(description = "状态（0-禁用，1-启用）")
    private Integer status;

    @Schema(description = "分组图标URL")
    private String iconUrl;

    @Schema(description = "分组内商品数量")
    private Integer spuCount;
}

