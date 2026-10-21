package com.deko.mall.cms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 视频管理列表 VO（列表不带 video_url 大字段，详情页带）
 */
@Data
@Schema(description = "视频管理列表VO")
public class VideoVO {

    @Schema(description = "视频ID")
    private Long id;

    @Schema(description = "视频标题")
    private String title;

    @Schema(description = "封面图URL")
    private String cover;

    @Schema(description = "简介")
    private String description;

    @Schema(description = "时长(秒)")
    private Integer duration;

    @Schema(description = "浏览数")
    private Integer accessCount;

    @Schema(description = "是否推荐(1是/0否)")
    private Integer isRecommended;

    @Schema(description = "是否热门(1是/0否)")
    private Integer isHot;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "是否启用(1是/0否)")
    private Integer isEnable;

    @Schema(description = "关联商品SPU ID(0/空=不挂)")
    private Long spuId;

    @Schema(description = "商品名称快照")
    private String spuName;

    @Schema(description = "商品主图快照")
    private String spuPic;

    @Schema(description = "商品价格快照")
    private java.math.BigDecimal spuPrice;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
