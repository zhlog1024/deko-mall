package com.deko.mall.ums.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.ums.model.entity.UmsMessage;

public interface UmsMessageService extends IService<UmsMessage> {

    IPage<UmsMessage> pageMessages(Long memberId, Page<UmsMessage> page);

    IPage<UmsMessage> pageMessagesAdmin(Page<UmsMessage> page, Long memberId, String title, Integer type, Integer isRead);

    Long countUnread(Long memberId);

    void sendMessage(Long memberId, String title, String detail, Integer type);

    int markAllRead(Long memberId);
}
