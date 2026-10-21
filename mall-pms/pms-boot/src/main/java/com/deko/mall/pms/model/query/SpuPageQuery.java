package com.deko.mall.pms.model.query;

import com.deko.mall.common.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 商品分页查询对象
 */

@Schema(description = "商品分页查询对象")
@Data
@EqualsAndHashCode(callSuper = true)
public class SpuPageQuery extends BasePageQuery {

    @Schema(description = "关键字")
    private String keywords;
    @Schema(description = "商品名称")
    private String name;
    @Schema(description = "商品分类ID")
    private Long categoryId;
    @Schema(description = "品牌ID")
    private Long brandId;
    @Schema(description = "最低价格（区间筛选）")
    private Long minPrice;
    @Schema(description = "最高价格（区间筛选，0表示不限）")
    private Long maxPrice;
    @Schema(description = "分类等级(1-一级,2-二级,3-三级)")
    private Integer categoryLevel;
    @Schema(description = "商品类型")
    private String spuType;
    /**
     * 商品类型合集
     */
    @Schema(description = "商品类型合集")
    private List<String> spuTypes;
    @Schema(description = "供应商ID")
    private Long supplierId;
    @Schema(description = "店铺ID(用户侧按店铺过滤)")
    private Long shopId;
    @Schema(description = "排序字段名")
    private String sortField;
    @Schema(description = "排序规则(asc:升序;desc:降序)")
    private String sort;
    @Schema(description = "审核状态(0-待审核，1-审核通过，2-拒绝)")
    private String auditStatus;
    @Schema(description = "商品状态 (0-草稿，1-上架，2-下架)")
    private Integer status;
    @Schema(description = "商品 ID")
    private Long spuId;
    @Schema(description = "规格 ID")
    private Long skuId;
}
