package com.deko.mall.pay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.deko.mall.pay.entity.PayOfflinePayment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 线下支付凭证审核 Mapper
 */
@Mapper
public interface PayOfflinePaymentMapper extends BaseMapper<PayOfflinePayment> {
}
