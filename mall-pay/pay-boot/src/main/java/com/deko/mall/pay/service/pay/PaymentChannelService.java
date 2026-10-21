package com.deko.mall.pay.service.pay;

import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.pay.entity.PayPaymentChannel;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentChannelService extends IService<PayPaymentChannel> {

    /**
     * 获取过期未支付的记录
     *
     * @param expireTime 过期时间
     * @return 过期未支付的记录列表
     */
    List<PayPaymentChannel> getExpiredRecords(LocalDateTime expireTime);

    /**
     * 根据订单号获取已支付的支付请求记录
     *
     * @param orderNo 订单号
     * @return 已支付的支付请求记录
     */
    PayPaymentChannel getPaidPaymentByOrderNo(String orderNo);
}
