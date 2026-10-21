package com.deko.mall.cms.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 视频评论实体（多级递归树：pid 指向父评论，0=顶级；支持图片评论单图 images）
 */
@Data
@TableName("cms_video_comment")
public class VideoComment {

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
     * 昵称快照（源 user_name_view 归一）
     */
    private String nickname;

    /**
     * 头像快照
     */
    private String avatar;

    /**
     * 父评论ID(0=顶级)
     */
    private Long pid;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 图片评论URL(单图)
     */
    private String images;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 直接子评论数
     */
    private Integer replyCount;

    /**
     * 是否启用(1是/0否)
     */
    private Integer isEnable;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 逻辑删除(0正常/1删除)
     */
    private Integer deleted;
}
