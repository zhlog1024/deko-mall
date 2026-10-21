package com.deko.mall.pms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 【应用端】商品详情视图对象
 */
@Data
@Schema(description = "商品详情")
public class SpuDetailVO {

    @Schema(description = "商品基本信息")
    private GoodsInfo goodsInfo;

    @Schema(description = "商品属性列表")
    private List<Attribute> attributeList;
    @Schema(description = "商品规格列表")
    private List<Specification> specList;
    @Schema(description = "商品库存单元列表")
    private List<Sku> skuList;
    @Schema(description = "店铺信息")
    private ShopInfo pluginsShopData;

    @Data
    @Schema(description = "店铺信息")
    public static class ShopInfo {

        @Schema(description = "店铺ID")
        private Long id;
        @Schema(description = "店铺名称")
        private String name;
        @Schema(description = "店铺logo")
        private String logo;
        @Schema(description = "店铺简介")
        private String describe;
        @Schema(description = "认证图标列表")
        private List<ShopIcon> iconList;
        @Schema(description = "店铺跳转地址")
        private String url;
    }

    @Data
    @Schema(description = "店铺认证图标")
    public static class ShopIcon {

        @Schema(description = "图标地址")
        private String icon;
        @Schema(description = "跳转地址")
        private String url;
    }

    @Data
    @Schema(description = "商品信息")
    public static class GoodsInfo {

        @Schema(description = "商品ID")
        private Long id;
        @Schema(description = "商品名称")
        private String name;
        @Schema(description = "商品原价（单位：元）")
        private BigDecimal originPrice;

        @Schema(description = "商品零售价（单位：元）")
        private BigDecimal price;

        @Schema(description = "销量")
        private Integer sales;
        @Schema(description = "商品图册")
        private List<String> album;
        @Schema(description = "商品详情")
        private String detail;
        @Schema(description = "是否可加入购物车")
        private Boolean canAddCart;
        @Schema(description = "商品类型")
        private String spuType;
        // 是否可以购买
        @Schema(description = "是否可以购买")
        private Boolean canBuy;
        @Schema(description = "是否已收藏")
        private Boolean isFavor;
    }

    @Data
    @Schema(description = "属性信息")
    public static class Attribute {
        @Schema(description = "属性ID")
        private Long id;
        @Schema(description = "属性名称")
        private String name;
        @Schema(description = "属性值")
        private String value;
    }

    @Data
    @Schema(description = "规格信息")
    public static class Specification {

        @Schema(description = "规格名称", example = "颜色")
        private String name;
        @Schema(description = "规格项列表", example = "黑,白")
        private List<Value> values;
        @Data
        @Schema(description = "规格项")
        public static class Value {
            @Schema(description = "规格项ID")
            private Long id;
            @Schema(description = "规格项值")
            private String value;
        }
    }

    @Data
    @Schema(description = "商品库存单元")
    public static class Sku {
        @Schema(description = "库存单元ID")
        private Long id;
        @Schema(description = "库存单元名称")
        private String name;
        @Schema(description = "库存单元规格值ID集合，以英文逗号拼接")
        private String specIds;
        @Schema(description = "价格")
        private BigDecimal price;

        @Schema(description = "市场价")
        private BigDecimal marketPrice;

        @Schema(description = "零售价")
        private BigDecimal retailPrice;
        @Schema(description = "库存")
        private Integer stock;
        @Schema(description = "商品图片URL")
        private String picUrl;
    }
}
