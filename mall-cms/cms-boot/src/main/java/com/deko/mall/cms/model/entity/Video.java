package com.deko.mall.cms.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 视频实体
 */
@Data
@TableName("cms_video")
@EqualsAndHashCode(callSuper = true)
public class Video extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 视频标题
     */
    private String title;
    /**
     * 封面图URL
     */
    private String cover;
    /**
     * 视频地址URL(mp4/hls)
     */
    private String videoUrl;
    /**
     * 时长(秒,默认0)
     */
    private Integer duration;
    /**
     * 简介
     */
    private String description;
    /**
     * 浏览数
     */
    private Integer accessCount;
    /**
     * 是否推荐(1是/0否)
     */
    private Integer isRecommended;
    /**
     * 是否热门(1是/0否)
     */
    private Integer isHot;
    /**
     * 排序(升序)
     */
    private Integer sort;
    /**
     * 是否启用(1是/0否)
     */
    private Integer isEnable;
    /**
     * 关联商品SPU ID(0/null=不挂)
     */
    private Long spuId;
    /**
     * 商品名称快照(管理端选品写入,后端不查 pms)
     */
    private String spuName;
    /**
     * 商品主图快照
     */
    private String spuPic;
    /**
     * 商品价格快照
     */
    private java.math.BigDecimal spuPrice;

    /**
     * 逻辑删除(0正常/1删除)
     */
    private Integer deleted;
}
