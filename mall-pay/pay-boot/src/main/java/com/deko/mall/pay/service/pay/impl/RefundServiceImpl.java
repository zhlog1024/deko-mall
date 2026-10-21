package com.deko.mall.pay.service.pay.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.pay.mapper.RefundMapper;
import com.deko.mall.pay.entity.PayRefund;
import com.deko.mall.pay.service.pay.RefundService;
import org.springframework.stereotype.Service;

@Service
public class RefundServiceImpl extends ServiceImpl<RefundMapper, PayRefund> implements RefundService {

    @Override
    public PayRefund getByRefundNo(String refundNo) {
        return lambdaQuery()
                .eq(PayRefund::getRefundNo, refundNo)
                .one();
    }
}


