package com.deko.mall.cms.controller.app;

import com.deko.mall.cms.model.vo.VideoPluginCommentVO;
import com.deko.mall.cms.service.VideoInteractService;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import com.deko.mall.common.security.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * APP端-视频插件契约接口（detail/lastnextdata/comments/givethumbs/report；游客读、登录互动）
 */
@Tag(name = "App-视频插件契约", description = "video 插件详情页竖滑 feed/评论树/点赞/举报")
@RestController
@RequestMapping("/app-api/v1/video-plugin")
@Slf4j
@RequiredArgsConstructor
public class VideoPluginController {

    private final VideoInteractService videoInteractService;

    @Operation(summary = "视频详情", description = "条目+举报类型+配置开关(常量默认全开)")
    @GetMapping("/detail")
    public Result<Map<String, Object>> detail(@RequestParam @NotNull Long id) {
        return Result.success(videoInteractService.pluginDetail(id, SecurityUtils.getMemberId()));
    }

    @Operation(summary = "上下滑补流", description = "返回 {last:[],next:[]} 双向视频流")
    @GetMapping("/lastnextdata")
    public Result<Map<String, Object>> lastNext(
            @RequestParam @NotNull Long id,
            @RequestParam(defaultValue = "0") Integer isLast,
            @RequestParam(defaultValue = "0") Integer isNext) {
        return Result.success(videoInteractService.lastNext(id, isLast == 1, isNext == 1, SecurityUtils.getMemberId()));
    }

    @Operation(summary = "评论列表", description = "顶层 pid=0 分页(多级递归由前端按需以 pid 逐层拉取)")
    @GetMapping("/comments")
    public PageResult<VideoPluginCommentVO> comments(
            @RequestParam @NotNull Long videoId,
            @RequestParam(required = false) Long pid,
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize) {
        return videoInteractService.comments(videoId, pid, pageNum, pageSize, SecurityUtils.getMemberId());
    }

    @Operation(summary = "发表评论/回复", description = "登录；pid=0 顶级评论,>0 回复(多级)；images 图片单图URL")
    @PostMapping("/comments")
    public Result<VideoPluginCommentVO> addComment(@Valid @RequestBody CommentBody body) {
        Long memberId = SecurityUtils.getMemberId();
        requireLogin(memberId);
        return Result.success(videoInteractService.addComment(
                body.getVideoId(), body.getPid() == null ? 0L : body.getPid(), body.getContent(), body.getImages(), memberId));
    }

    @Operation(summary = "删除评论", description = "本人删除(级联删子评论)")
    @DeleteMapping("/comments/{commentId}")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        Long memberId = SecurityUtils.getMemberId();
        requireLogin(memberId);
        videoInteractService.deleteComment(commentId, memberId);
        return Result.success();
    }

    @Operation(summary = "点赞/取消", description = "videoId 必填；commentId 空=赞视频,非空=赞评论；返回 {count,is_active}")
    @PostMapping("/givethumbs")
    public Result<Map<String, Object>> giveThumbs(@Valid @RequestBody ThumbsBody body) {
        Long memberId = SecurityUtils.getMemberId();
        requireLogin(memberId);
        return Result.success(videoInteractService.giveThumbs(body.getVideoId(), body.getCommentId(), memberId));
    }

    @Operation(summary = "举报视频", description = "登录；type 取 detail 返回的 report_type_list")
    @PostMapping("/report")
    public Result<Void> report(@Valid @RequestBody ReportBody body) {
        Long memberId = SecurityUtils.getMemberId();
        requireLogin(memberId);
        videoInteractService.report(body.getVideoId(), body.getType(), body.getContent(), memberId);
        return Result.success();
    }

    private void requireLogin(Long memberId) {
        if (memberId == null) {
            throw new com.deko.mall.common.web.exception.BizException("请先登录");
        }
    }

    @Data
    public static class CommentBody {
        @NotNull
        private Long videoId;
        private Long pid;
        @NotBlank
        private String content;
        private String images;
    }

    @Data
    public static class ThumbsBody {
        @NotNull
        private Long videoId;
        private Long commentId;
    }

    @Data
    public static class ReportBody {
        @NotNull
        private Long videoId;
        @NotBlank
        private String type;
        private String content;
    }
}
