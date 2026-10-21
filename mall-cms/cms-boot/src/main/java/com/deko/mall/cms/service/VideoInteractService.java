package com.deko.mall.cms.service;

import com.deko.mall.cms.model.vo.VideoPluginCommentVO;
import com.deko.mall.cms.model.vo.VideoPluginItemVO;
import com.deko.mall.common.result.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 视频互动服务
 */
public interface VideoInteractService {

    /**
     * 视频详情（条目 + 互动计数 + 关联商品 + 配置；游客 memberId=null）
     */
    Map<String, Object> pluginDetail(Long videoId, Long memberId);

    /**
     * 上下滑补流：返回 {last:[], next:[]}（条目同 detail.data 结构）
     */
    Map<String, Object> lastNext(Long videoId, boolean isLast, boolean isNext, Long memberId);

    /**
     * 评论列表（树，分页顶层 + 递归子级；pid>0 返回该评论子级）
     */
    PageResult<VideoPluginCommentVO> comments(Long videoId, Long pid, Integer pageNum, Integer pageSize, Long memberId);

    /**
     * 发表评论/回复（多级递归，pid 任意层；支持图片 images 单图）
     */
    VideoPluginCommentVO addComment(Long videoId, Long pid, String content, String images, Long memberId);

    /**
     * 点赞/取消（type=1 视频 / type=2 评论）
     */
    Map<String, Object> giveThumbs(Long videoId, Long commentId, Long memberId);

    /**
     * 删除评论（本人）
     */
    void deleteComment(Long commentId, Long memberId);

    /**
     * 举报视频
     */
    void report(Long videoId, String type, String content, Long memberId);
}
