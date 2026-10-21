package com.deko.mall.ums.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.ums.mapper.UmsMessageMapper;
import com.deko.mall.ums.model.entity.UmsMessage;
import com.deko.mall.ums.service.UmsMessageService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UmsMessageServiceImpl extends ServiceImpl<UmsMessageMapper, UmsMessage> implements UmsMessageService {

    @Override
    public IPage<UmsMessage> pageMessages(Long memberId, Page<UmsMessage> page) {
        LambdaQueryWrapper<UmsMessage> wrapper = new LambdaQueryWrapper<UmsMessage>()
                .eq(UmsMessage::getUserDeleted, 0)
                .eq(UmsMessage::getDeleted, 0)
                .and(w -> w.eq(UmsMessage::getMemberId, memberId).or().eq(UmsMessage::getMemberId, 0L))
                .orderByDesc(UmsMessage::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public IPage<UmsMessage> pageMessagesAdmin(Page<UmsMessage> page, Long memberId, String title, Integer type, Integer isRead) {
        LambdaQueryWrapper<UmsMessage> wrapper = new LambdaQueryWrapper<UmsMessage>()
                .eq(UmsMessage::getDeleted, 0);
        if (memberId != null) {
            wrapper.eq(UmsMessage::getMemberId, memberId);
        }
        if (StringUtils.hasText(title)) {
            wrapper.like(UmsMessage::getTitle, title);
        }
        if (type != null) {
            wrapper.eq(UmsMessage::getType, type);
        }
        if (isRead != null) {
            wrapper.eq(UmsMessage::getIsRead, isRead);
        }
        wrapper.orderByDesc(UmsMessage::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public Long countUnread(Long memberId) {
        return this.lambdaQuery()
                .eq(UmsMessage::getMemberId, memberId)
                .eq(UmsMessage::getIsRead, 0)
                .eq(UmsMessage::getUserDeleted, 0)
                .eq(UmsMessage::getDeleted, 0)
                .count();
    }

    @Override
    public void sendMessage(Long memberId, String title, String detail, Integer type) {
        UmsMessage message = new UmsMessage();
        message.setMemberId(memberId != null ? memberId : 0L);
        message.setTitle(title);
        message.setDetail(detail);
        message.setType(type != null ? type : 1);
        message.setIsRead(0);
        message.setUserDeleted(0);
        message.setDeleted(0);
        this.save(message);
    }

    @Override
    public int markAllRead(Long memberId) {
        return this.baseMapper.update(null,
                new LambdaUpdateWrapper<UmsMessage>()
                        .eq(UmsMessage::getMemberId, memberId)
                        .eq(UmsMessage::getIsRead, 0)
                        .set(UmsMessage::getIsRead, 1));
    }
}
