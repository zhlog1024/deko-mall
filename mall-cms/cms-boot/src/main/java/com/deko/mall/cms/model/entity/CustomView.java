package com.deko.mall.cms.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 自定义页面实体
 */
@Data
@TableName("cms_custom_view")
@EqualsAndHashCode(callSuper = true)
public class CustomView extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * logo图片URL
     */
    private String logo;
    /**
     * 页面名称
     */
    private String name;
    /**
     * 页面HTML代码(整页内容,app 用 mp-html 渲染)
     */
    private String htmlContent;
    /**
     * CSS样式(预留;app 端 mp-html 不执行,web 用)
     */
    private String cssContent;
    /**
     * JS代码(预留;app 端不执行,web 用)
     */
    private String jsContent;
    /**
     * 是否启用(1是/0否)
     */
    private Integer isEnable;
    /**
     * 是否包含头部(1是/0否)
     */
    private Integer isHeader;
    /**
     * 是否包含尾部(1是/0否)
     */
    private Integer isFooter;
    /**
     * 是否满屏(1是/0否)
     */
    private Integer isFullScreen;
    /**
     * 访问量
     */
    private Integer accessCount;
    /**
     * SEO标题
     */
    private String seoTitle;
    /**
     * SEO关键字
     */
    private String seoKeywords;
    /**
     * SEO描述
     */
    private String seoDesc;
    /**
     * 逻辑删除(0正常/1删除)
     */
    private Integer deleted;
}
