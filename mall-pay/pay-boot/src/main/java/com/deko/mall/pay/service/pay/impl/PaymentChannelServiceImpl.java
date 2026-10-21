package com.deko.mall.pay.service.pay.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.pay.mapper.PaymentChannelMapper;
import com.deko.mall.pay.entity.PayPaymentChannel;
import com.deko.mall.pay.service.pay.PaymentChannelService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 渠道支付请求数据服务实现
 * 提供 oms_payment_request 表的 CRUD 操作实现
 */
@Service
public class PaymentChannelServiceImpl extends ServiceImpl<PaymentChannelMapper, PayPaymentChannel> implements PaymentChannelService {

    @Override
    public List<PayPaymentChannel> getExpiredRecords(LocalDateTime expireTime) {
        return lambdaQuery()
                .lt(PayPaymentChannel::getExpireTime, expireTime)
                .eq(PayPaymentChannel::getStatus, 0) // 未支付状态
                .list();
    }

    @Override
    public PayPaymentChannel getPaidPaymentByOrderNo(String orderNo) {
        // 由于PaymentRequest不再有orderNo字段，这个方法现在通过paymentNo查询
        // orderNo参数实际上是paymentNo
        return lambdaQuery()
                .eq(PayPaymentChannel::getPaymentNo, orderNo)
                .eq(PayPaymentChannel::getStatus, 1) // 已支付状态
                .orderByDesc(PayPaymentChannel::getUpdateTime)
                .last("limit 1")
                .one();
    }
}
