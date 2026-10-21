package com.deko.mall.cms.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * DIY 首页按端配置实体
 * 每端(client_type)一行，指定首页 DIY 页面 + 可选定时区间
 */
@Data
@TableName("cms_diy_home_mode")
@EqualsAndHashCode(callSuper = true)
public class DiyHomeMode extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 端类型：weixin/app/h5
     */
    private String clientType;
    /**
     * 首页 DIY 页面 ID
     */
    private Long diyPageId;
    /**
     * 生效开始时间（可空）
     */
    private LocalDateTime timeStart;

    /**
     * 生效结束时间（可空）
     */
    private LocalDateTime timeEnd;

    /**
     * 逻辑删除 0-正常 1-删除
     */
    private Integer deleted;
}
