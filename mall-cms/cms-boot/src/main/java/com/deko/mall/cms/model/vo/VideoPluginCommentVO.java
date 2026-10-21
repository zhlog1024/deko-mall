package com.deko.mall.cms.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 视频评论树节点（comment-info 消费：user{user_name_view,avatar}、
 * sub_comments 多级递归、like_count/is_give_thumbs/comments_count/page 等）
 */
@Data
@Schema(description = "视频评论树节点")
public class VideoPluginCommentVO {

    @Schema(description = "评论ID")
    private Long id;
    @Schema(description = "所属视频ID")
    private Long videoId;
    @Schema(description = "评论内容")
    private String content;
    @Schema(description = "图片评论URL(单图,可空)")
    private String images;
    @Schema(description = "父评论ID(0=顶级)")
    private Long pid;
    @Schema(description = "评论用户")
    private VideoPluginUserVO user;

    @Schema(description = "点赞数")
    private Integer likeCount;
    @Schema(description = "是否已赞(登录态)")
    private Integer isGiveThumbs;
    @Schema(description = "评论点赞数(与 likeCount 同源,源字段兼容)")
    private Integer giveThumbsCount;
    @Schema(description = "直接回复数")
    private Integer commentsCount;
    @Schema(description = "子评论(多级递归,懒加载:未展开时为空)")
    private List<VideoPluginCommentVO> subComments = new ArrayList<>();

    @Schema(description = "是否显示子评论(前端控制)")
    private Boolean showSubComment;
    @Schema(description = "子评论当前页/总页(懒加载分页)")
    private Integer page;
    private Integer pageTotal;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Data
    @Schema(description = "评论用户(源 user_name_view)")
    public static class VideoPluginUserVO {
        private Long id;
        private String user_name_view;
        private String avatar;
    }
}
