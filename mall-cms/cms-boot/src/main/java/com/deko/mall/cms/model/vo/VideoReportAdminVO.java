package com.deko.mall.cms.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 视频举报管理 VO
 */
@Data
@Schema(description = "视频举报管理VO")
public class VideoReportAdminVO {

    private Long id;
    private Long videoId;
    private Long userId;
    private String type;
    private String content;
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "视频标题(冗余展示)")
    private String videoTitle;
}
