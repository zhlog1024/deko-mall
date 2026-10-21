package com.deko.mall.pay.service.pay;

import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.pay.entity.PayRefund;

public interface RefundService extends IService<PayRefund> {

    /**
     * 根据退款单号获取退款记录
     *
     * @param refundNo 退款单号
     * @return 退款记录
     */
    PayRefund getByRefundNo(String refundNo);
}


