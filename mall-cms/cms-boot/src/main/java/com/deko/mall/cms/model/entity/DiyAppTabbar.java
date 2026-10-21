package com.deko.mall.cms.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DIY 底部导航装修数据实体
 * 底部导航装修配置按业务标识存储
 */
@Data
@TableName("cms_diy_app_tabbar")
@EqualsAndHashCode(callSuper = true)
public class DiyAppTabbar extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 业务标识（当前仅 home）
     */
    private String business;
    /**
     * 底部导航装修配置 JSON（content/style 结构）
     */
    private String config;
    /**
     * 是否启用 1/0
     */
    private Integer isEnable;
    /**
     * 逻辑删除 0-正常 1-删除
     */
    private Integer deleted;
}
