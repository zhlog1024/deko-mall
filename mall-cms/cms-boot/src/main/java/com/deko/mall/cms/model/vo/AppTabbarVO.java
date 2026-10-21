package com.deko.mall.cms.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * App 底部导航配置 VO
 */
@Data
@Schema(description = "App底部导航配置")
public class AppTabbarVO {

    @Schema(description = "内容配置")
    private Content content;

    @Schema(description = "样式配置")
    private Style style;

    /**
     * 内容配置
     */
    @Data
    @Schema(description = "底部导航内容配置")
    public static class Content {
        
        @Schema(description = "导航样式(0图片加文字, 1图片, 2文字)")
        @JsonProperty("nav_style")
        private String navStyle;
        @Schema(description = "导航类型(0底部固定, 1底部悬浮)")
        @JsonProperty("nav_type")
        private String navType;
        @Schema(description = "导航内容列表")
        @JsonProperty("nav_content")
        private List<NavItem> navContent;
    }

    /**
     * 导航项
     */
    @Data
    @Schema(description = "底部导航项")
    public static class NavItem {
        
        @Schema(description = "唯一标识")
        private String id;
        @Schema(description = "名称")
        private String name;
        @Schema(description = "默认图标")
        private List<ImageItem> img;
        @Schema(description = "选中图标")
        @JsonProperty("img_checked")
        private List<ImageItem> imgChecked;
        @Schema(description = "链接配置")
        private Link link;

        @Schema(description = "角标数字(可选)")
        private Integer badge;
    }

    /**
     * 图片项
     */
    @Data
    @Schema(description = "图片项")
    public static class ImageItem {
        
        @Schema(description = "图片ID")
        private Integer id;
        @Schema(description = "图片URL")
        private String url;
        @Schema(description = "原始URL")
        private String original;
        @Schema(description = "标题")
        private String title;
        @Schema(description = "扩展名")
        private String ext;
        @Schema(description = "类型")
        private String type;
    }

    /**
     * 链接配置
     */
    @Data
    @Schema(description = "链接配置")
    public static class Link {
        
        @Schema(description = "链接名称")
        private String name;
        @Schema(description = "页面路径")
        private String page;
    }

    /**
     * 样式配置
     */
    @Data
    @Schema(description = "底部导航样式配置")
    public static class Style {
        
        @Schema(description = "选中文本颜色")
        @JsonProperty("text_color_checked")
        private String textColorChecked;
        @Schema(description = "默认文本颜色")
        @JsonProperty("default_text_color")
        private String defaultTextColor;
        @Schema(description = "通用样式")
        @JsonProperty("common_style")
        private CommonStyle commonStyle;
    }

    /**
     * 通用样式
     */
    @Data
    @Schema(description = "通用样式")
    public static class CommonStyle {
        
        @Schema(description = "上内边距")
        @JsonProperty("padding_top")
        private Integer paddingTop;
        @Schema(description = "下内边距")
        @JsonProperty("padding_bottom")
        private Integer paddingBottom;
        @Schema(description = "左内边距")
        @JsonProperty("padding_left")
        private Integer paddingLeft;
        @Schema(description = "右内边距")
        @JsonProperty("padding_right")
        private Integer paddingRight;
        @Schema(description = "上外边距")
        @JsonProperty("margin_top")
        private Integer marginTop;
        @Schema(description = "下外边距")
        @JsonProperty("margin_bottom")
        private Integer marginBottom;
        @Schema(description = "背景色列表")
        @JsonProperty("color_list")
        private List<ColorItem> colorList;
        @Schema(description = "渐变方向")
        private String direction;
        @Schema(description = "背景图片样式")
        @JsonProperty("background_img_style")
        private String backgroundImgStyle;
        @Schema(description = "背景图片列表")
        @JsonProperty("background_img")
        private List<ImageItem> backgroundImg;
    }

    /**
     * 颜色项
     */
    @Data
    @Schema(description = "颜色项")
    public static class ColorItem {
        
        @Schema(description = "颜色值")
        private String color;
        @Schema(description = "颜色百分比")
        @JsonProperty("color_percentage")
        private Integer colorPercentage;
    }
}
