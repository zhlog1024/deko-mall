package com.deko.mall.cms.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 视频评论管理 VO
 */
@Data
@Schema(description = "视频评论管理VO")
public class VideoCommentAdminVO {

    private Long id;
    private Long videoId;
    private Long userId;
    private String nickname;
    private String avatar;
    private Long pid;
    private String content;
    private String images;
    private Integer likeCount;
    private Integer replyCount;
    private Integer isEnable;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "视频标题(冗余展示)")
    private String videoTitle;
}
