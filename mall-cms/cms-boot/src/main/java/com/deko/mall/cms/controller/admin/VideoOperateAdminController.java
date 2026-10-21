package com.deko.mall.cms.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.deko.mall.cms.model.vo.VideoCommentAdminVO;
import com.deko.mall.cms.model.vo.VideoReportAdminVO;
import com.deko.mall.cms.service.VideoOperateService;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端-视频运营（评论审核/删除、举报查看/处理）
 */
@Tag(name = "admin-视频运营管理接口")
@RestController
@RequestMapping("/api/v1/video-operate")
@RequiredArgsConstructor
public class VideoOperateAdminController {

    private final VideoOperateService videoOperateService;

    @Operation(summary = "评论分页")
    @GetMapping("/comments")
    @PreAuthorize("@ss.hasPerm('cms:video:comment')")
    public PageResult<VideoCommentAdminVO> pageComments(
            @RequestParam(required = false) Long videoId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer isEnable,
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize) {
        IPage<VideoCommentAdminVO> result = videoOperateService.pageComments(videoId, keyword, isEnable, pageNum, pageSize);
        return PageResult.success(result);
    }

    @Operation(summary = "评论删除(运营)")
    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("@ss.hasPerm('cms:video:comment')")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        videoOperateService.deleteComment(commentId);
        return Result.success();
    }

    @Operation(summary = "评论启停")
    @PutMapping("/comments/{commentId}/enable")
    @PreAuthorize("@ss.hasPerm('cms:video:comment')")
    public Result<Void> enableComment(@PathVariable Long commentId, @RequestParam Integer isEnable) {
        videoOperateService.enableComment(commentId, isEnable);
        return Result.success();
    }

    @Operation(summary = "举报分页")
    @GetMapping("/reports")
    @PreAuthorize("@ss.hasPerm('cms:video:report')")
    public PageResult<VideoReportAdminVO> pageReports(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize) {
        IPage<VideoReportAdminVO> result = videoOperateService.pageReports(keyword, status, pageNum, pageSize);
        return PageResult.success(result);
    }

    @Operation(summary = "举报处理状态更新")
    @PutMapping("/reports/{reportId}/handle")
    @PreAuthorize("@ss.hasPerm('cms:video:report')")
    public Result<Void> handleReport(@PathVariable Long reportId, @RequestParam(defaultValue = "1") Integer status) {
        videoOperateService.handleReport(reportId, status);
        return Result.success();
    }
}
