package com.deko.mall.pay.service.pay.strategy.impl;

import com.deko.mall.common.web.exception.BizException;
import com.deko.mall.pay.api.enums.PayStatusEnum;
import com.deko.mall.pay.api.dto.PaymentResultDTO;
import com.deko.mall.pay.config.PayResultCode;
import com.deko.mall.pay.entity.PayPaymentChannel;
import com.deko.mall.pay.service.pay.strategy.PayStrategy;
import lombok.extern.slf4j.Slf4j;

/**
 * 支付策略抽象基类
 */
@Slf4j
public abstract class AbstractPayStrategy implements PayStrategy {

    /**
     * 验证支付请求记录
     */
    protected void validatePayRecord(PayPaymentChannel paymentRequest) {
        if (paymentRequest == null) {
            throw new BizException(PayResultCode.PAY_REQUEST_NOT_FOUND);
        }
        if (!PayStatusEnum.WAIT_PAY.getValue().equals(paymentRequest.getStatus())) {
            throw new BizException(PayResultCode.PAY_REQUEST_STATUS_INVALID);
        }
    }

    /**
     * 创建支付成功的回调结果
     */
    protected PaymentResultDTO createSuccessCallback(String paymentChannelNo, String channelTradeNo, String notifyData) {
        PaymentResultDTO result = new PaymentResultDTO();
        result.setPaymentChannelNo(paymentChannelNo);
        result.setPaymentNo(null); // 策略层不设置业务支付单号
        result.setChannelTradeNo(channelTradeNo);
        result.setStatus(PayStatusEnum.PAY_SUCCESS.getValue());
        result.setNotifyData(notifyData);
        return result;
    }

    /**
     * 创建支付失败的回调结果
     */
    protected PaymentResultDTO createFailCallback(String paymentChannelNo, String channelTradeNo, String notifyData) {
        PaymentResultDTO result = new PaymentResultDTO();
        result.setPaymentChannelNo(paymentChannelNo);
        result.setPaymentNo(null); // 策略层不设置业务支付单号
        result.setChannelTradeNo(channelTradeNo);
        result.setStatus(PayStatusEnum.PAY_FAIL.getValue());
        result.setNotifyData(notifyData);
        return result;
    }


    /**
     * 记录支付日志
     */
    protected void logPayInfo(String operation, String paymentChannelNo, Object... args) {
        log.info("支付操作：{}，支付单号：{}，参数：{}", operation, paymentChannelNo, args);
    }

}
