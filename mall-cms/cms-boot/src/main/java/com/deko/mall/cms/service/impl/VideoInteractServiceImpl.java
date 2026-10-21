package com.deko.mall.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deko.mall.cms.mapper.VideoCommentMapper;
import com.deko.mall.cms.mapper.VideoLikeMapper;
import com.deko.mall.cms.mapper.VideoMapper;
import com.deko.mall.cms.mapper.VideoReportMapper;
import com.deko.mall.cms.model.entity.Video;
import com.deko.mall.cms.model.entity.VideoComment;
import com.deko.mall.cms.model.entity.VideoLike;
import com.deko.mall.cms.model.entity.VideoReport;
import com.deko.mall.cms.model.vo.VideoPluginCommentVO;
import com.deko.mall.cms.model.vo.VideoPluginItemVO;
import com.deko.mall.cms.service.VideoInteractService;
import com.deko.mall.common.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 视频互动服务实现
 * <p>
 * 评论多级递归树：comments 顶层分页，子级按 pid 递归查询（每层直接子级，
 * 客户端按需展开懒加载）；点赞视频/评论共用 cms_video_like(type 区分)。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class VideoInteractServiceImpl implements VideoInteractService {

    private final VideoMapper videoMapper;
    private final VideoLikeMapper videoLikeMapper;
    private final VideoCommentMapper videoCommentMapper;
    private final VideoReportMapper videoReportMapper;

    private static final int LIKE_TYPE_VIDEO = 1;
    private static final int LIKE_TYPE_COMMENT = 2;

    @Override
    public Map<String, Object> pluginDetail(Long videoId, Long memberId) {
        Map<String, Object> result = new LinkedHashMap<>();
        Video video = requireVideo(videoId);
        // base_config_data：常量默认全开（用户确认，不加配置表）
        Map<String, Object> baseConfig = defaultBaseConfig();
        // report_type_list：举报类型常量
        List<Map<String, Object>> reportTypes = reportTypeList();

        VideoPluginItemVO item = toPluginItem(video, memberId);
        // 顶层评论：详情不预载，前端按需 data 端点拉取（commentsList 留空）
        result.put("data", item);
        result.put("report_type_list", reportTypes);
        result.put("base_config_data", baseConfig);
        result.put("editor_path_type", "video");
        return result;
    }

    @Override
    public Map<String, Object> lastNext(Long videoId, boolean isLast, boolean isNext, Long memberId) {
        Map<String, Object> result = new LinkedHashMap<>();
        Video cur = requireVideo(videoId);
        List<VideoPluginItemVO> last = new ArrayList<>();
        List<VideoPluginItemVO> next = new ArrayList<>();
        // 按 id 序取相邻（sort 一致的简单实现：id 倒序上一批/正序下一批各取 1 条邻近）
        if (isLast) {
            List<Video> prev = videoMapper.selectList(new LambdaQueryWrapper<Video>()
                    .eq(Video::getIsEnable, 1)
                    .eq(Video::getDeleted, 0)
                    .lt(Video::getId, cur.getId())
                    .orderByDesc(Video::getId)
                    .last("limit 5"));
            for (Video v : prev) {
                last.add(toPluginItem(v, memberId));
            }
            // 倒序为正序展示（数组头为最近上一条）
            java.util.Collections.reverse(last);
        }
        if (isNext) {
            List<Video> fol = videoMapper.selectList(new LambdaQueryWrapper<Video>()
                    .eq(Video::getIsEnable, 1)
                    .eq(Video::getDeleted, 0)
                    .gt(Video::getId, cur.getId())
                    .orderByAsc(Video::getId)
                    .last("limit 5"));
            for (Video v : fol) {
                next.add(toPluginItem(v, memberId));
            }
        }
        result.put("last", last);
        result.put("next", next);
        return result;
    }

    @Override
    public PageResult<VideoPluginCommentVO> comments(Long videoId, Long pid, Integer pageNum, Integer pageSize, Long memberId) {
        int page = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int size = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 50);
        Page<VideoComment> p = new Page<>(page, size);
        LambdaQueryWrapper<VideoComment> wrapper = new LambdaQueryWrapper<VideoComment>()
                .eq(VideoComment::getVideoId, videoId)
                .eq(VideoComment::getIsEnable, 1)
                .eq(VideoComment::getDeleted, 0)
                .eq(pid != null && pid > 0, VideoComment::getPid, pid == null ? 0 : pid)
                .orderByAsc(VideoComment::getCreateTime);
        if (pid == null || pid <= 0) {
            wrapper.eq(VideoComment::getPid, 0L);
        }
        Page<VideoComment> result = videoCommentMapper.selectPage(p, wrapper);
        List<VideoPluginCommentVO> vos = result.getRecords().stream()
                .map(c -> toCommentVO(c, memberId)).toList();
        Page<VideoPluginCommentVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(vos);
        return PageResult.success(voPage);
    }

    @Override
    public VideoPluginCommentVO addComment(Long videoId, Long pid, String content, String images, Long memberId) {
        requireVideo(videoId);
        if (!StringUtils.hasText(content)) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        VideoComment comment = new VideoComment();
        comment.setVideoId(videoId);
        comment.setUserId(memberId);
        comment.setNickname(null); // token 无昵称,前端兜底
        comment.setAvatar(null);
        comment.setPid(pid == null ? 0L : pid);
        comment.setContent(content.trim());
        comment.setImages(StringUtils.hasText(images) ? images : null);
        comment.setLikeCount(0);
        comment.setReplyCount(0);
        comment.setIsEnable(1);
        comment.setDeleted(0);
        videoCommentMapper.insert(comment);
        // 父评论 reply_count+1（若为回复）
        if (pid != null && pid > 0) {
            VideoComment parent = videoCommentMapper.selectById(pid);
            if (parent != null && parent.getDeleted() != null && parent.getDeleted() == 0) {
                VideoComment upd = new VideoComment();
                upd.setId(parent.getId());
                upd.setReplyCount((parent.getReplyCount() == null ? 0 : parent.getReplyCount()) + 1);
                videoCommentMapper.updateById(upd);
            }
        }
        return toCommentVO(comment, memberId);
    }

    @Override
    public Map<String, Object> giveThumbs(Long videoId, Long commentId, Long memberId) {
        int type = (commentId != null && commentId > 0) ? LIKE_TYPE_COMMENT : LIKE_TYPE_VIDEO;
        long targetId = commentId == null ? 0L : commentId;
        LambdaQueryWrapper<VideoLike> wrapper = new LambdaQueryWrapper<VideoLike>()
                .eq(VideoLike::getVideoId, videoId)
                .eq(VideoLike::getUserId, memberId)
                .eq(VideoLike::getType, type)
                .eq(VideoLike::getCommentId, targetId);
        VideoLike like = videoLikeMapper.selectOne(wrapper);
        boolean active;
        if (like != null) {
            videoLikeMapper.deleteById(like.getId());
            active = false;
        } else {
            VideoLike n = new VideoLike();
            n.setVideoId(videoId);
            n.setUserId(memberId);
            n.setType(type);
            n.setCommentId(targetId);
            videoLikeMapper.insert(n);
            active = true;
        }
        long count = videoLikeMapper.selectCount(new LambdaQueryWrapper<VideoLike>()
                .eq(VideoLike::getVideoId, videoId)
                .eq(VideoLike::getType, type)
                .eq(VideoLike::getCommentId, targetId));
        // 同步评论表 like_count（type=2）
        if (type == LIKE_TYPE_COMMENT) {
            VideoComment c = videoCommentMapper.selectById(targetId);
            if (c != null) {
                VideoComment upd = new VideoComment();
                upd.setId(c.getId());
                upd.setLikeCount((int) count);
                videoCommentMapper.updateById(upd);
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("is_active", active);
        return result;
    }

    @Override
    public void deleteComment(Long commentId, Long memberId) {
        VideoComment comment = videoCommentMapper.selectById(commentId);
        if (comment == null || comment.getDeleted() == 1) {
            throw new IllegalArgumentException("评论不存在");
        }
        // 仅本人或已处理：memberId 校验
        if (memberId == null || !memberId.equals(comment.getUserId())) {
            throw new IllegalArgumentException("无权删除该评论");
        }
        comment.setDeleted(1);
        videoCommentMapper.updateById(comment);
        // 递归删除子评论（避免孤儿树）——简单做法:按 pid 级联删一层即可,深层保留无害
        deleteChildrenRecursive(commentId);
    }

    private void deleteChildrenRecursive(Long parentId) {
        List<VideoComment> children = videoCommentMapper.selectList(new LambdaQueryWrapper<VideoComment>()
                .eq(VideoComment::getPid, parentId)
                .eq(VideoComment::getDeleted, 0));
        for (VideoComment child : children) {
            child.setDeleted(1);
            videoCommentMapper.updateById(child);
            deleteChildrenRecursive(child.getId());
        }
    }

    @Override
    public void report(Long videoId, String type, String content, Long memberId) {
        requireVideo(videoId);
        VideoReport report = new VideoReport();
        report.setVideoId(videoId);
        report.setUserId(memberId);
        report.setType(StringUtils.hasText(type) ? type : "其他");
        report.setContent(content);
        report.setStatus(0);
        report.setDeleted(0);
        videoReportMapper.insert(report);
    }

    // 内部

    private Video requireVideo(Long videoId) {
        Video video = videoMapper.selectById(videoId);
        if (video == null || video.getDeleted() == 1 || video.getIsEnable() != 1) {
            throw new IllegalArgumentException("视频不存在或已下线");
        }
        return video;
    }

    private VideoPluginItemVO toPluginItem(Video video, Long memberId) {
        VideoPluginItemVO vo = new VideoPluginItemVO();
        BeanUtils.copyProperties(video, vo);
        vo.setGiveThumbsCount(countLike(video.getId(), LIKE_TYPE_VIDEO, 0L));
        vo.setIsGiveThumbs(memberId != null && isLiked(video.getId(), memberId, LIKE_TYPE_VIDEO, 0L));
        vo.setCommentsCount(countComments(video.getId()));
        // 关联商品快照
        if (video.getSpuId() != null && video.getSpuId() > 0) {
            VideoPluginItemVO.VideoPluginGoodsVO goods = new VideoPluginItemVO.VideoPluginGoodsVO();
            goods.setId(video.getSpuId());
            goods.setTitle(video.getSpuName());
            goods.setImages(video.getSpuPic());
            BigDecimal price = video.getSpuPrice();
            goods.setPrice(price == null ? null : price.toPlainString());
            vo.setGoods(goods);
            vo.setShowGoods(true);
        } else {
            vo.setShowGoods(false);
        }
        return vo;
    }

    private VideoPluginCommentVO toCommentVO(VideoComment c, Long memberId) {
        VideoPluginCommentVO vo = new VideoPluginCommentVO();
        BeanUtils.copyProperties(c, vo);
        vo.setLikeCount(c.getLikeCount());
        vo.setGiveThumbsCount(c.getLikeCount());
        vo.setIsGiveThumbs(memberId != null && isLiked(c.getVideoId(), memberId, LIKE_TYPE_COMMENT, c.getId()) ? 1 : 0);
        vo.setCommentsCount(c.getReplyCount() == null ? 0 : c.getReplyCount());
        vo.setShowSubComment(false);
        vo.setPage(0);
        vo.setPageTotal(0);
        VideoPluginCommentVO.VideoPluginUserVO user = new VideoPluginCommentVO.VideoPluginUserVO();
        user.setId(c.getUserId());
        user.setUser_name_view(StringUtils.hasText(c.getNickname()) ? c.getNickname()
                : (c.getUserId() == null ? "匿名用户" : "用户" + String.valueOf(c.getUserId() % 1000000)));
        user.setAvatar(c.getAvatar());
        vo.setUser(user);
        return vo;
    }

    private int countLike(Long videoId, int type, Long commentId) {
        Long n = videoLikeMapper.selectCount(new LambdaQueryWrapper<VideoLike>()
                .eq(VideoLike::getVideoId, videoId)
                .eq(VideoLike::getType, type)
                .eq(VideoLike::getCommentId, commentId));
        return n == null ? 0 : n.intValue();
    }

    private boolean isLiked(Long videoId, Long memberId, int type, Long commentId) {
        if (memberId == null) {
            return false;
        }
        Long n = videoLikeMapper.selectCount(new LambdaQueryWrapper<VideoLike>()
                .eq(VideoLike::getVideoId, videoId)
                .eq(VideoLike::getUserId, memberId)
                .eq(VideoLike::getType, type)
                .eq(VideoLike::getCommentId, commentId));
        return n != null && n > 0;
    }

    private int countComments(Long videoId) {
        Long n = videoCommentMapper.selectCount(new LambdaQueryWrapper<VideoComment>()
                .eq(VideoComment::getVideoId, videoId)
                .eq(VideoComment::getIsEnable, 1)
                .eq(VideoComment::getDeleted, 0));
        return n == null ? 0 : n.intValue();
    }

    private Map<String, Object> defaultBaseConfig() {
        Map<String, Object> cfg = new HashMap<>();
        cfg.put("is_video_give_thumbs", 1);
        cfg.put("is_video_comments_show", 1);
        cfg.put("is_video_comments_add", 1);
        cfg.put("is_video_detail_show_goods", 1);
        cfg.put("is_video_detail_show_goods_modal", 1);
        return cfg;
    }

    private List<Map<String, Object>> reportTypeList() {
        // 源 detail 页消费结构：分组 {name:主原因, data:[具体类型]}
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> group = new HashMap<>();
        group.put("name", "请选择举报原因");
        List<String> sub = new ArrayList<>();
        sub.add("色情低俗");
        sub.add("侵权盗版");
        sub.add("诈骗信息");
        sub.add("违法内容");
        sub.add("其他");
        group.put("data", sub);
        list.add(group);
        return list;
    }
}
