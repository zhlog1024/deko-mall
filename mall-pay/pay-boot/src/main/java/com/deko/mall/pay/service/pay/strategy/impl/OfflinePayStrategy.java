package com.deko.mall.pay.service.pay.strategy.impl;

import cn.hutool.json.JSONUtil;
import com.deko.mall.pay.api.dto.PaymentResultDTO;
import com.deko.mall.pay.api.enums.PayChannelEnum;
import com.deko.mall.pay.api.enums.PayStatusEnum;
import com.deko.mall.pay.entity.PayConfig;
import com.deko.mall.pay.entity.PayOfflinePayment;
import com.deko.mall.pay.entity.PayPaymentChannel;
import com.deko.mall.pay.entity.PayRefundChannel;
import com.deko.mall.pay.mapper.PayOfflinePaymentMapper;
import com.deko.mall.pay.model.vo.PaymentResponseVO;
import com.deko.mall.pay.service.pay.strategy.PayStrategy;
import com.deko.mall.pay.service.pay.PaymentChannelService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 线下支付策略
 * <p>
 * 线下支付不需要调用第三方API，用户上传转账凭证后等待管理员审核。
 * paymentRequest 阶段：创建 PayOfflinePayment 审核记录，凭证 URL 存入 payParam。
 * 审核通过由 Admin 接口调用 handlePaySuccess 完成后续流程。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OfflinePayStrategy implements PayStrategy {

    private final PayOfflinePaymentMapper offlinePaymentMapper;
    private final PaymentChannelService paymentChannelService;

    @Override
    public void initStrategy(PayConfig config) {
        log.info("线下支付策略初始化完成");
    }

    @Override
    public PaymentResponseVO paymentRequest(PayPaymentChannel paymentRequest) {
        log.info("线下支付策略 - 开始处理支付请求, paymentNo={}, paymentChannelNo={}",
                paymentRequest.getPaymentNo(), paymentRequest.getPaymentChannelNo());

        // 从 payParam 中提取 voucherUrls
        String payParam = paymentRequest.getPayParam();
        OfflinePaySubmitData submitData;
        try {
            submitData = JSONUtil.toBean(payParam, OfflinePaySubmitData.class);
        } catch (Exception e) {
            log.error("线下支付 payParam 解析失败, paymentChannelNo={}", paymentRequest.getPaymentChannelNo(), e);
            throw new IllegalArgumentException("凭证数据格式错误");
        }

        if (submitData == null || submitData.getVoucherUrls() == null || submitData.getVoucherUrls().isEmpty()) {
            throw new IllegalArgumentException("请上传支付凭证");
        }

        // 创建审核记录
        PayOfflinePayment offlinePayment = new PayOfflinePayment();
        offlinePayment.setPaymentNo(paymentRequest.getPaymentNo());
        offlinePayment.setPaymentChannelNo(paymentRequest.getPaymentChannelNo());
        offlinePayment.setVoucherUrls(JSONUtil.toJsonStr(submitData.getVoucherUrls()));
        offlinePayment.setAuditStatus(0);
        offlinePayment.setResubmitCount(0);
        offlinePaymentMapper.insert(offlinePayment);

        log.info("线下支付凭证已提交, paymentNo={}, paymentChannelNo={}, voucherCount={}",
                paymentRequest.getPaymentNo(), paymentRequest.getPaymentChannelNo(),
                submitData.getVoucherUrls().size());

        return PaymentResponseVO.builder()
                .payNo(paymentRequest.getPaymentChannelNo())
                .payStatus(PayStatusEnum.PAYING.getValue())
                .payParam(JSONUtil.toJsonStr(submitData))
                .build();
    }

    @Override
    public PaymentResultDTO paymentResult(HttpServletRequest request) {
        return null;
    }

    @Override
    public PaymentResponseVO queryStatus(String paymentChannelNo) {
        PayPaymentChannel channel = paymentChannelService.lambdaQuery()
                .eq(PayPaymentChannel::getPaymentChannelNo, paymentChannelNo)
                .one();
        if (channel == null) {
            return PaymentResponseVO.builder()
                    .payStatus(PayStatusEnum.PAY_FAIL.getValue())
                    .errorMsg("支付记录不存在")
                    .build();
        }
        return PaymentResponseVO.builder()
                .payNo(paymentChannelNo)
                .payStatus(channel.getStatus())
                .build();
    }

    @Override
    public boolean closePayment(String paymentChannelNo) {
        log.info("线下支付策略 - 关闭支付, paymentChannelNo={}", paymentChannelNo);
        PayPaymentChannel channel = paymentChannelService.lambdaQuery()
                .eq(PayPaymentChannel::getPaymentChannelNo, paymentChannelNo)
                .one();
        if (channel != null) {
            channel.setStatus(PayStatusEnum.CLOSED.getValue());
            paymentChannelService.updateById(channel);
        }
        return true;
    }

    @Override
    public boolean refund(PayRefundChannel refundRequest) {
        log.info("线下支付策略 - 退款, refundNo={}", refundRequest.getPaymentChannelNo());
        return true;
    }

    @Override
    public String getPayChannel() {
        return PayChannelEnum.OFFLINE_PAY.getValue();
    }

    /**
     * 线下支付提交数据结构（用于 payParam 序列化）
     */
    @lombok.Data
    public static class OfflinePaySubmitData {
        private java.util.List<String> voucherUrls;
    }
}
