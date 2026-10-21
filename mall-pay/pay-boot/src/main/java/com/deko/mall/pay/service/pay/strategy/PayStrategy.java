package com.deko.mall.pay.service.pay.strategy;

import com.deko.mall.pay.api.dto.PaymentResultDTO;
import com.deko.mall.pay.api.dto.RefundResultDTO;
import com.deko.mall.pay.entity.PayConfig;
import com.deko.mall.pay.entity.PayPaymentChannel;
import com.deko.mall.pay.entity.PayRefundChannel;
import com.deko.mall.pay.model.vo.PaymentResponseVO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 支付策略接口
 */
public interface PayStrategy {
    /**
     * 初始化支付策略
     *
     * @param config 支付配置
     */
    void initStrategy(PayConfig config);

    /**
     * 创建支付请求
     *
     * @param paymentRequest 支付请求记录
     * @return 支付参数
     */
    PaymentResponseVO paymentRequest(PayPaymentChannel paymentRequest);

    /**
     * 处理支付回调
     *
     * @return 处理结果
     */
    PaymentResultDTO paymentResult(HttpServletRequest request);

    /**
     * 查询支付状态
     *
     * @param paymentChannelNo 支付单号
     * @return 支付状态
     */
    PaymentResponseVO queryStatus(String paymentChannelNo);

    /**
     * 关闭支付单
     *
     * @param paymentChannelNo 支付单号
     * @return 关闭结果
     */
    boolean closePayment(String paymentChannelNo);

    /**
     * 申请退款
     *
     * @param refundRequest 退款请求对象
     * @return 退款结果
     */
    boolean refund(PayRefundChannel refundRequest);


    /**
     * 获取支付渠道编码
     */
    String getPayChannel();

    /**
     * 处理退款回调
     * 接收并处理支付渠道的退款结果通知
     *
     * @param request HTTP请求对象，包含退款回调数据
     * @return 退款结果DTO，包含退款单号、状态等信息
     */
    default RefundResultDTO refundCallback(HttpServletRequest request) {
        return null;
    }
}
