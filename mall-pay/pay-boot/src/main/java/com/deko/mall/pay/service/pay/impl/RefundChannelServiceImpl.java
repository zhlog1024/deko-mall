package com.deko.mall.pay.service.pay.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.pay.mapper.RefundChannelMapper;
import com.deko.mall.pay.entity.PayRefundChannel;
import com.deko.mall.pay.service.pay.RefundChannelService;
import org.springframework.stereotype.Service;

@Service
public class RefundChannelServiceImpl extends ServiceImpl<RefundChannelMapper, PayRefundChannel> implements RefundChannelService {
}
