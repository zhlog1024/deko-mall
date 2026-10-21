package com.deko.mall.pms.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 商品分组实体
 * 用于页面装修系统中的商品分组管理，如"精选"、"母婴"、"家清"、"生鲜"等
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("pms_spu_group")
public class PmsSpuGroup extends BaseEntity {

    /**
     * 分组ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 分组名称
     */
    private String name;

    /**
     * 分组描述
     */
    private String description;

    /**
     * 展示顺序
     */
    private Integer sort;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;

    /**
     * 分组图标URL
     */
    private String iconUrl;

    /**
     * 是否系统内置（0-否，1-是）
     */
    private Integer isSystem;
}

