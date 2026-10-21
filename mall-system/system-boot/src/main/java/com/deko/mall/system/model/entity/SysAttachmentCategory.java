package com.deko.mall.system.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;

/**
 * 附件分类表
 */
@Data
@TableName("sys_attachment_category")
public class SysAttachmentCategory extends BaseEntity {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 父级ID（0表示根分类）
     */
    private Long parentId;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 是否启用（1:启用;0:禁用）
     */
    private Integer isEnabled;
}

