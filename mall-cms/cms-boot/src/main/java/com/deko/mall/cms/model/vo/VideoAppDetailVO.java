package com.deko.mall.cms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 视频 App 详情 VO（含视频地址/简介，浏览数自增）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "视频App详情VO")
public class VideoAppDetailVO extends VideoAppItemVO {

    @Schema(description = "视频地址URL(mp4/hls)")
    private String videoUrl;

    @Schema(description = "简介")
    private String description;
}
