package com.deko.mall.ums.controller.app;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deko.mall.common.base.BasePageQuery;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import com.deko.mall.common.security.util.SecurityUtils;
import com.deko.mall.ums.model.entity.UmsMessage;
import com.deko.mall.ums.service.UmsMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "App-消息管理")
@RestController
@RequestMapping("/app-api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final UmsMessageService messageService;

    @Operation(summary = "消息分页列表")
    @PostMapping
    public PageResult<UmsMessage> listMessages(@RequestBody BasePageQuery query) {
        Long memberId = SecurityUtils.getMemberId();
        // 先标记已读，再返回列表
        messageService.markAllRead(memberId);
        Page<UmsMessage> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<UmsMessage> result = messageService.pageMessages(memberId, page);
        return PageResult.success(result);
    }

    @Operation(summary = "未读消息数")
    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        Long memberId = SecurityUtils.getMemberId();
        Long count = messageService.countUnread(memberId);
        return Result.success(count);
    }

    @Operation(summary = "全部标记已读")
    @PutMapping("/read-all")
    public Result<Void> readAll() {
        Long memberId = SecurityUtils.getMemberId();
        messageService.markAllRead(memberId);
        return Result.success();
    }
}
