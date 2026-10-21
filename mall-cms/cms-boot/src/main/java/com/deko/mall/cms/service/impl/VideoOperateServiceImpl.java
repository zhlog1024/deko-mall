package com.deko.mall.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deko.mall.cms.mapper.VideoCommentMapper;
import com.deko.mall.cms.mapper.VideoMapper;
import com.deko.mall.cms.mapper.VideoReportMapper;
import com.deko.mall.cms.model.entity.Video;
import com.deko.mall.cms.model.entity.VideoComment;
import com.deko.mall.cms.model.entity.VideoReport;
import com.deko.mall.cms.model.vo.VideoCommentAdminVO;
import com.deko.mall.cms.model.vo.VideoReportAdminVO;
import com.deko.mall.cms.service.VideoOperateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 视频运营管理服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class VideoOperateServiceImpl implements VideoOperateService {

    private final VideoCommentMapper videoCommentMapper;
    private final VideoReportMapper videoReportMapper;
    private final VideoMapper videoMapper;

    @Override
    public IPage<VideoCommentAdminVO> pageComments(Long videoId, String keyword, Integer isEnable,
                                                   Integer pageNum, Integer pageSize) {
        int page = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int size = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 50);
        LambdaQueryWrapper<VideoComment> wrapper = new LambdaQueryWrapper<VideoComment>()
                .eq(videoId != null && videoId > 0, VideoComment::getVideoId, videoId)
                .eq(isEnable != null, VideoComment::getIsEnable, isEnable)
                .eq(VideoComment::getDeleted, 0)
                .and(StringUtils.hasText(keyword), w -> w.like(VideoComment::getContent, keyword)
                        .or().like(VideoComment::getNickname, keyword))
                .orderByDesc(VideoComment::getCreateTime);
        Page<VideoComment> p = new Page<>(page, size);
        Page<VideoComment> result = videoCommentMapper.selectPage(p, wrapper);
        Page<VideoCommentAdminVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<VideoCommentAdminVO> vos = result.getRecords().stream().map(c -> {
            VideoCommentAdminVO vo = new VideoCommentAdminVO();
            vo.setId(c.getId());
            vo.setVideoId(c.getVideoId());
            vo.setUserId(c.getUserId());
            vo.setNickname(c.getNickname());
            vo.setAvatar(c.getAvatar());
            vo.setPid(c.getPid());
            vo.setContent(c.getContent());
            vo.setImages(c.getImages());
            vo.setLikeCount(c.getLikeCount());
            vo.setReplyCount(c.getReplyCount());
            vo.setIsEnable(c.getIsEnable());
            vo.setCreateTime(c.getCreateTime());
            return vo;
        }).toList();
        voPage.setRecords(vos);
        // 冗余视频标题
        fillVideoTitles(vos);
        return voPage;
    }

    private void fillVideoTitles(List<VideoCommentAdminVO> comments) {
        if (comments == null || comments.isEmpty()) {
            return;
        }
        Map<Long, String> titleMap = comments.stream()
                .map(VideoCommentAdminVO::getVideoId)
                .distinct()
                .map(videoMapper::selectById)
                .filter(v -> v != null)
                .collect(Collectors.toMap(Video::getId, Video::getTitle));
        comments.forEach(c -> c.setVideoTitle(titleMap.get(c.getVideoId())));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId) {
        VideoComment comment = videoCommentMapper.selectById(commentId);
        if (comment == null || (comment.getDeleted() != null && comment.getDeleted() == 1)) {
            throw new IllegalArgumentException("评论不存在");
        }
        comment.setDeleted(1);
        videoCommentMapper.updateById(comment);
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
    public void enableComment(Long commentId, Integer isEnable) {
        VideoComment comment = videoCommentMapper.selectById(commentId);
        if (comment == null || (comment.getDeleted() != null && comment.getDeleted() == 1)) {
            throw new IllegalArgumentException("评论不存在");
        }
        VideoComment upd = new VideoComment();
        upd.setId(commentId);
        upd.setIsEnable(isEnable == null ? 1 : isEnable);
        videoCommentMapper.updateById(upd);
    }

    @Override
    public IPage<VideoReportAdminVO> pageReports(String keyword, Integer status, Integer pageNum, Integer pageSize) {
        int page = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int size = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 50);
        LambdaQueryWrapper<VideoReport> wrapper = new LambdaQueryWrapper<VideoReport>()
                .eq(status != null, VideoReport::getStatus, status)
                .eq(VideoReport::getDeleted, 0)
                .and(StringUtils.hasText(keyword), w -> w.like(VideoReport::getType, keyword)
                        .or().like(VideoReport::getContent, keyword))
                .orderByAsc(VideoReport::getStatus)
                .orderByDesc(VideoReport::getCreateTime);
        Page<VideoReport> p = new Page<>(page, size);
        Page<VideoReport> result = videoReportMapper.selectPage(p, wrapper);
        Page<VideoReportAdminVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<VideoReportAdminVO> vos = result.getRecords().stream().map(r -> {
            VideoReportAdminVO vo = new VideoReportAdminVO();
            vo.setId(r.getId());
            vo.setVideoId(r.getVideoId());
            vo.setUserId(r.getUserId());
            vo.setType(r.getType());
            vo.setContent(r.getContent());
            vo.setStatus(r.getStatus());
            vo.setCreateTime(r.getCreateTime());
            return vo;
        }).toList();
        voPage.setRecords(vos);
        fillReportVideoTitles(vos);
        return voPage;
    }

    private void fillReportVideoTitles(List<VideoReportAdminVO> reports) {
        if (reports == null || reports.isEmpty()) {
            return;
        }
        Map<Long, String> titleMap = reports.stream()
                .map(VideoReportAdminVO::getVideoId)
                .distinct()
                .map(videoMapper::selectById)
                .filter(v -> v != null)
                .collect(Collectors.toMap(Video::getId, Video::getTitle));
        reports.forEach(r -> r.setVideoTitle(titleMap.get(r.getVideoId())));
    }

    @Override
    public void handleReport(Long reportId, Integer status) {
        VideoReport report = videoReportMapper.selectById(reportId);
        if (report == null || (report.getDeleted() != null && report.getDeleted() == 1)) {
            throw new IllegalArgumentException("举报记录不存在");
        }
        VideoReport upd = new VideoReport();
        upd.setId(reportId);
        upd.setStatus(status == null ? 1 : status);
        videoReportMapper.updateById(upd);
    }
}
