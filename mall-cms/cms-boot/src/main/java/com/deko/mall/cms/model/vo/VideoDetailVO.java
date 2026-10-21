package com.deko.mall.cms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 视频详情 VO（管理端编辑回显，含视频地址/简介）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "视频详情VO")
public class VideoDetailVO extends VideoVO {

    @Schema(description = "视频地址URL(mp4/hls)")
    private String videoUrl;
}
