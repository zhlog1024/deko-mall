package com.deko.mall.pms.model.query;

import com.deko.mall.common.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品审核分页查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SpuReviewPageQuery extends BasePageQuery {

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "供应商名称")
    private String supplierName;

    @Schema(description = "商品类型")
    private String spuType;

    @Schema(description = "审核状态")
    private String auditStatus;

    @Schema(description = "分类ID")
    private Long categoryId;
}
