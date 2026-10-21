package com.deko.mall.cms.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文章实体
 */
@Data
@TableName("cms_article")
@EqualsAndHashCode(callSuper = true)
public class Article extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 标题
     */
    private String title;
    /**
     * 简介/描述
     */
    private String description;
    /**
     * 分类id
     */
    private Long categoryId;
    /**
     * 标题颜色(十六进制)
     */
    private String titleColor;
    /**
     * 外链跳转地址(非空时详情跳外链)
     */
    private String jumpUrl;
    /**
     * 是否启用(1是/0否)
     */
    private Integer isEnable;
    /**
     * 正文HTML
     */
    private String content;
    /**
     * 封面URL
     */
    private String cover;
    /**
     * 图集JSON(一维url数组)
     */
    private String images;
    /**
     * 访问量
     */
    private Integer accessCount;
    /**
     * 分享图URL
     */
    private String shareImages;
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
