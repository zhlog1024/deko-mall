package com.deko.mall.pms.model.query;

import com.deko.mall.common.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品分组查询参数
 */
@Data
@Schema(description = "商品分组查询参数")
@EqualsAndHashCode(callSuper = true)
public class PmsSpuGroupQuery extends BasePageQuery {


    @Schema(description = "分组名称")
    private String name;

    @Schema(description = "分组ID")
    private Long groupId;

    @Schema(description = "排序字段，默认为销量", example = "sales")
    private String sortField = "sales";

    @Schema(description = "排序方式，asc-升序，desc-降序", example = "desc")
    private String sortOrder = "desc";

    @Schema(description = "状态（0-禁用，1-启用）")
    private Integer status;
}

