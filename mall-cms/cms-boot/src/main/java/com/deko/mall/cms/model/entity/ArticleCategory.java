package com.deko.mall.cms.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文章分类实体（树形,pid 关联）
 */
@Data
@TableName("cms_article_category")
@EqualsAndHashCode(callSuper = true)
public class ArticleCategory extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 父分类id(0=顶级)
     */
    private Long pid;
    /**
     * 分类名称
     */
    private String name;
    /**
     * 排序(升序)
     */
    private Integer sort;
    /**
     * 是否启用(1是/0否)
     */
    private Integer isEnable;
    /**
     * 逻辑删除(0正常/1删除)
     */
    private Integer deleted;
}
