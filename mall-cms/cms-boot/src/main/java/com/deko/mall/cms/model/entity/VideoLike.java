package com.deko.mall.cms.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 视频点赞实体（视频/评论双对象；表无 update_time/deleted，不继承 BaseEntity）
 */
@Data
@TableName("cms_video_like")
public class VideoLike {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 视频ID
     */
    private Long videoId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 点赞对象类型(1=视频/2=评论)
     */
    private Integer type;

    /**
     * type=2 时评论ID
     */
    private Long commentId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
