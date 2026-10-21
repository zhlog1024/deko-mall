package com.deko.mall.pms.controller.app;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.deko.mall.common.result.Result;
import com.deko.mall.pms.model.form.CommentSubmitForm;
import com.deko.mall.pms.model.vo.CommentScoreVO;
import com.deko.mall.pms.model.vo.GoodsCommentVO;
import com.deko.mall.pms.service.GoodsCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * App-商品评论接口
 */
@Tag(name = "App-商品评论", description = "提供APP端商品评论相关接口")
@RestController
@RequestMapping("/app-api/v1")
@RequiredArgsConstructor
@Slf4j
public class AppCommentController {

    private final GoodsCommentService commentService;

    @Operation(summary = "获取商品评论列表")
    @GetMapping("/goods/{spuId}/comments")
    public Result<IPage<GoodsCommentVO>> pageComments(
            @Parameter(description = "商品ID") @PathVariable Long spuId,
            @Parameter(description = "评分筛选") @RequestParam(required = false) Integer rating,
            @Parameter(description = "是否有图") @RequestParam(required = false) Boolean hasImage,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<GoodsCommentVO> result = commentService.pageAppComments(spuId, rating, hasImage, pageNum, pageSize);
        return Result.success(result);
    }

    @Operation(summary = "获取商品评分统计")
    @GetMapping("/goods/{spuId}/comment-score")
    public Result<CommentScoreVO> getCommentScore(
            @Parameter(description = "商品ID") @PathVariable Long spuId) {
        return Result.success(commentService.getCommentScore(spuId));
    }

    @Operation(summary = "提交订单评论")
    @PostMapping("/orders/{orderId}/comments")
    public Result<Void> submitComments(
            @Parameter(description = "订单ID") @PathVariable Long orderId,
            @Valid @RequestBody CommentSubmitForm form) {
        commentService.submitComments(orderId, form);
        return Result.success();
    }

    @Operation(summary = "获取我的评论列表")
    @GetMapping("/member/comments/page")
    public Result<IPage<GoodsCommentVO>> pageMemberComments(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(commentService.pageMemberComments(pageNum, pageSize));
    }

    @Operation(summary = "删除我的评论")
    @DeleteMapping("/member/comments/{id}")
    public Result<Void> deleteMyComment(@Parameter(description = "评论ID") @PathVariable Long id) {
        commentService.deleteMyComment(id);
        return Result.success();
    }
}
