package com.deko.mall.pay.service.pay.strategy.impl;

import com.deko.mall.common.web.exception.BizException;
import com.deko.mall.pay.api.enums.PayChannelEnum;
import com.deko.mall.pay.config.PayResultCode;
import com.deko.mall.pay.api.enums.PayStatusEnum;
import com.deko.mall.pay.api.dto.PaymentResultDTO;
import com.deko.mall.pay.entity.PayConfig;
import com.deko.mall.pay.entity.PayPaymentChannel;
import com.deko.mall.pay.entity.PayRefundChannel;
import com.deko.mall.pay.model.vo.PaymentResponseVO;
import com.deko.mall.pay.service.pay.strategy.PayStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 全额支付策略
 * 专门处理0元单的全额积分抵扣支付
 * <p>
 * 遵循与其他支付策略相同的架构：
 * - paymentRequest: 创建支付单，返回支付参数
 * - paymentResult: 处理回调（全额抵扣不支持HTTP回调）
 * - 真正的订单状态更新由PayServiceImpl.handlePaySuccess处理
 */
@Slf4j
@Component
public class FullDeductionPayStrategy implements PayStrategy {

    @Override
    public void initStrategy(PayConfig config) {
        // 全额支付不需要复杂的配置初始化
        log.info("全额抵扣策略初始化完成");
    }

    @Override
    public PaymentResponseVO paymentRequest(PayPaymentChannel paymentRequest) {
        log.info("全额抵扣策略 - 开始处理支付请求，请求号：{}，金额：{}",
                paymentRequest.getPaymentChannelNo(), paymentRequest.getAmount());

        try {
            // 全额支付是特殊的同步支付，直接返回支付成功参数
            // 不限制金额，因为业务层已经决定了使用全抵扣渠道
            log.info("全额抵扣策略 - 直接返回成功参数，请求号：{}，金额：{}",
                    paymentRequest.getPaymentChannelNo(), paymentRequest.getAmount());

            return PaymentResponseVO.builder()
                    .payNo(paymentRequest.getPaymentChannelNo())
                    .payParam("{\"paid\":true,\"message\":\"全额抵扣支付成功\"}")
                    .payStatus(PayStatusEnum.PAY_SUCCESS.getValue())
                    .build();

        } catch (Exception e) {
            log.error("全额抵扣处理失败，请求号：{}，错误：{}", paymentRequest.getPaymentChannelNo(), e.getMessage(), e);
            throw new BizException(PayResultCode.PAY_FULL_DEDUCTION_FAILED, e.getMessage());
        }
    }

    @Override
    public PaymentResultDTO paymentResult(jakarta.servlet.http.HttpServletRequest request) {
        // 全额支付不支持HTTP回调，因为它是同步处理的
        // 这个方法主要用于接口兼容性
        log.warn("全额抵扣策略 - 不支持HTTP回调方式");
        return null;
    }

    @Override
    public PaymentResponseVO queryStatus(String paymentChannelNo) {
        log.info("全额抵扣策略 - 查询支付状态，支付单号：{}", paymentChannelNo);

        // 全额支付一旦发起就应该是成功的
        // 这里为了接口兼容性，统一返回成功状态
        return PaymentResponseVO.builder()
                .payNo(paymentChannelNo)
                .payStatus(PayStatusEnum.PAY_SUCCESS.getValue())
                .build();
    }

    @Override
    public boolean closePayment(String paymentChannelNo) {
        log.info("全额抵扣策略 - 关闭支付，支付单号：{}", paymentChannelNo);

        // 全额支付不支持关闭操作，因为它是同步完成的
        log.info("全额抵扣无需关闭，支付单号：{}", paymentChannelNo);
        return true;
    }

    @Override
    public boolean refund(PayRefundChannel refundRequest) {
        log.info("全额抵扣策略 - 支持退款功能，退款请求号：{}", refundRequest.getRefundChannelNo());
        // 全额支付支持退款，积分退回由订单服务处理
        // 这里返回true表示退款申请成功，实际的积分退回在OrderService中处理
        return true;
    }

    @Override
    public String getPayChannel() {
        return PayChannelEnum.FULL_DEDUCTION.getValue();
    }
}
