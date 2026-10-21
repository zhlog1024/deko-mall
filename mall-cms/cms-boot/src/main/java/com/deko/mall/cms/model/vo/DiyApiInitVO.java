package com.deko.mall.cms.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * DIY 装修器初始化数据 VO
 */
@Data
@Schema(description = "DIY装修器初始化数据")
public class DiyApiInitVO {

    @Schema(description = "配置数据")
    private DiyConfigVO config;

    @Schema(description = "附件分类列表")
    @JsonProperty("attachment_category")
    private List<CategoryTreeVO> attachmentCategory;
    @Schema(description = "文章分类列表")
    @JsonProperty("article_category")
    private List<CategoryTreeVO> articleCategory;
    @Schema(description = "品牌分类列表")
    @JsonProperty("brand_category")
    private List<CategoryTreeVO> brandCategory;
    @Schema(description = "品牌列表")
    @JsonProperty("brand_list")
    private List<BrandVO> brandList;
    @Schema(description = "商品分类列表")
    @JsonProperty("goods_category")
    private List<CategoryTreeVO> goodsCategory;
    @Schema(description = "页面链接列表")
    @JsonProperty("page_link_list")
    private List<PageLinkVO> pageLinkList;
    @Schema(description = "模块组件列表")
    @JsonProperty("module_list")
    private List<ModuleGroupVO> moduleList;
    @Schema(description = "品牌排序类型列表")
    @JsonProperty("brand_order_by_type_list")
    private List<OrderByTypeVO> brandOrderByTypeList;
    @Schema(description = "文章排序类型列表")
    @JsonProperty("article_order_by_type_list")
    private List<OrderByTypeVO> articleOrderByTypeList;
    @Schema(description = "商品排序类型列表")
    @JsonProperty("goods_order_by_type_list")
    private List<OrderByTypeVO> goodsOrderByTypeList;
    @Schema(description = "数据排序规则列表")
    @JsonProperty("data_order_by_rule_list")
    private List<OrderByTypeVO> dataOrderByRuleList;
    @Schema(description = "插件列表")
    private List<PluginVO> plugins;
}
