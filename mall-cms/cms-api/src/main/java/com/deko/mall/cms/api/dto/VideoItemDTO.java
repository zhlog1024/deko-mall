package com.deko.mall.cms.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 视频 Feign 数据项（供装修器 plugins-video-list/tabs 渲染等外部消费）
 * 字段语义video 组件数据契约：标题/封面/视频地址/时长/浏览数等
 */
@Data
@Schema(description = "视频数据项")
public class VideoItemDTO implements Serializable {

    @Schema(description = "视频ID")
    private Long id;
    @Schema(description = "视频标题")
    private String title;
    @Schema(description = "封面图URL")
    private String cover;
    @Schema(description = "视频地址URL(mp4/hls)")
    private String videoUrl;
    @Schema(description = "时长(秒)")
    private Integer duration;
    @Schema(description = "浏览数")
    private Integer accessCount;
    @Schema(description = "是否推荐(1是/0否)")
    private Integer isRecommended;
    @Schema(description = "是否热门(1是/0否)")
    private Integer isHot;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
