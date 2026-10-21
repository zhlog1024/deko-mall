package com.deko.mall.pms.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import com.deko.mall.pms.model.form.CommentAuditForm;
import com.deko.mall.pms.model.form.CommentReplyForm;
import com.deko.mall.pms.model.query.CommentPageQuery;
import com.deko.mall.pms.model.vo.GoodsCommentVO;
import com.deko.mall.pms.service.GoodsCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * Admin-商品评论管理
 */
@Tag(name = "Admin-商品评论管理")
@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final GoodsCommentService commentService;

    @Operation(summary = "评论分页列表")
    @GetMapping("/page")
    public PageResult<GoodsCommentVO> page(CommentPageQuery query) {
        IPage<GoodsCommentVO> result = commentService.pageComments(query);
        return PageResult.success(result);
    }

    @Operation(summary = "评论详情")
    @GetMapping("/{id}")
    public Result<GoodsCommentVO> detail(@Parameter(description = "评论ID") @PathVariable Long id) {
        return Result.success(commentService.getCommentDetail(id));
    }

    @Operation(summary = "回复评论")
    @PutMapping("/{id}/reply")
    public Result<Void> reply(
            @Parameter(description = "评论ID") @PathVariable Long id,
            @RequestBody CommentReplyForm form) {
        commentService.reply(id, form);
        return Result.success();
    }

    @Operation(summary = "审核评论")
    @PutMapping("/{id}/audit")
    public Result<Void> audit(
            @Parameter(description = "评论ID") @PathVariable Long id,
            @RequestBody CommentAuditForm form) {
        commentService.audit(id, form);
        return Result.success();
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@Parameter(description = "评论ID") @PathVariable Long id) {
        commentService.deleteComment(id);
        return Result.success();
    }
}
