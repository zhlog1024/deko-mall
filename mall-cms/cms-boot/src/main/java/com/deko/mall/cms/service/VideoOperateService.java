package com.deko.mall.cms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.deko.mall.cms.model.vo.VideoCommentAdminVO;
import com.deko.mall.cms.model.vo.VideoReportAdminVO;

/**
 * 视频运营管理服务（评论审核/删除、举报查看/处理）
 */
public interface VideoOperateService {

    /**
     * 评论分页（videoId 可选；keyword 模糊昵称/内容）
     */
    IPage<VideoCommentAdminVO> pageComments(Long videoId, String keyword, Integer isEnable, Integer pageNum, Integer pageSize);

    /**
     * 评论删除（运营，逻辑删；级联删子评论）
     */
    void deleteComment(Long commentId);

    /**
     * 评论启停
     */
    void enableComment(Long commentId, Integer isEnable);

    /**
     * 举报分页（status 0待处理/1已处理 可选）
     */
    IPage<VideoReportAdminVO> pageReports(String keyword, Integer status, Integer pageNum, Integer pageSize);

    /**
     * 举报处理状态更新（0→1 已处理）
     */
    void handleReport(Long reportId, Integer status);
}
