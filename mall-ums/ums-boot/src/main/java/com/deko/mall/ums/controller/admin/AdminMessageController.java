package com.deko.mall.ums.controller.admin;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import com.deko.mall.ums.model.entity.UmsMessage;
import com.deko.mall.ums.service.UmsMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Admin-消息管理")
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class AdminMessageController {

    private final UmsMessageService messageService;

    @Operation(summary = "消息分页列表")
    @GetMapping
    public PageResult<UmsMessage> listMessages(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Long pageSize,
            @Parameter(description = "会员ID") @RequestParam(required = false) Long memberId,
            @Parameter(description = "标题关键字") @RequestParam(required = false) String title,
            @Parameter(description = "消息类型") @RequestParam(required = false) Integer type,
            @Parameter(description = "已读状态") @RequestParam(required = false) Integer isRead
    ) {
        Page<UmsMessage> page = new Page<>(pageNum, pageSize);
        IPage<UmsMessage> result = messageService.pageMessagesAdmin(page, memberId, title, type, isRead);
        return PageResult.success(result);
    }

    @Operation(summary = "消息详情")
    @GetMapping("/{id}")
    public Result<UmsMessage> getMessage(@PathVariable Long id) {
        UmsMessage message = messageService.getById(id);
        return Result.success(message);
    }

    @Operation(summary = "发送消息")
    @PostMapping
    public Result<Void> sendMessage(@RequestBody Map<String, Object> params) {
        Long memberId = params.get("memberId") != null ? Long.valueOf(params.get("memberId").toString()) : null;
        String title = (String) params.get("title");
        String detail = (String) params.get("detail");
        Integer type = params.get("type") != null ? Integer.valueOf(params.get("type").toString()) : 1;
        messageService.sendMessage(memberId, title, detail, type);
        return Result.success();
    }

    @Operation(summary = "删除消息")
    @DeleteMapping("/{id}")
    public Result<Void> deleteMessage(@PathVariable Long id) {
        messageService.update(new LambdaUpdateWrapper<UmsMessage>()
                .eq(UmsMessage::getId, id)
                .set(UmsMessage::getDeleted, 1));
        return Result.success();
    }

    @Operation(summary = "批量删除消息")
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteMessages(@RequestBody List<Long> ids) {
        messageService.update(new LambdaUpdateWrapper<UmsMessage>()
                .in(UmsMessage::getId, ids)
                .set(UmsMessage::getDeleted, 1));
        return Result.success();
    }
}
