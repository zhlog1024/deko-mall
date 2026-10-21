package com.deko.mall.cms.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DIY 页面实体
 */
@Data
@TableName("cms_diy_page")
public class DiyPage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 页面名称
     */
    private String name;

    /**
     * 装修 JSON 配置
     */
    private String config;

    /**
     * 封面图
     */
    private String coverUrl;

    /**
     * 是否首页
     */
    private Boolean isHome;

    /**
     * 是否启用（发布）
     */
    private Boolean isEnabled;

    /**
     * 数据签名
     */
    private String md5Key;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

