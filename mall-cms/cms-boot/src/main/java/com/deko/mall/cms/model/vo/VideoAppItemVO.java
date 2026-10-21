package com.deko.mall.cms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 视频 App 列表项 VO（用户端渲染，不带 video_url/description 大字段）
 */
@Data
@Schema(description = "视频App列表项VO")
public class VideoAppItemVO {

    @Schema(description = "视频ID")
    private Long id;

    @Schema(description = "视频标题")
    private String title;

    @Schema(description = "封面图URL")
    private String cover;

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
