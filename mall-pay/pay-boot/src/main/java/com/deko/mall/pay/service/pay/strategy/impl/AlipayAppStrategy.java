package com.deko.mall.pay.service.pay.strategy.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.ijpay.alipay.AliPayApi;
import com.ijpay.alipay.AliPayApiConfig;
import com.ijpay.alipay.AliPayApiConfigKit;
import com.deko.mall.common.web.exception.BizException;
import com.deko.mall.pay.api.enums.PayChannelEnum;
import com.deko.mall.pay.api.enums.PayStatusEnum;
import com.deko.mall.pay.api.dto.PaymentResultDTO;
import com.deko.mall.pay.api.dto.RefundResultDTO;
import com.deko.mall.pay.api.enums.RefundStatusEnum;
import com.deko.mall.pay.entity.AlipayExtConfig;
import com.deko.mall.pay.entity.PayConfig;
import com.deko.mall.pay.entity.PayPaymentChannel;
import com.deko.mall.pay.entity.PayRefundChannel;
import com.deko.mall.pay.model.vo.PaymentResponseVO;
import com.deko.mall.pay.service.pay.strategy.PayStrategy;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import com.deko.mall.pay.config.PayResultCode;

/**
 * 支付宝APP支付策略 - 使用IJPay实现
 */
@Slf4j
@Component
public class AlipayAppStrategy extends AbstractPayStrategy implements PayStrategy {

    private PayConfig currentConfig;
    private AlipayExtConfig extConfig;

    private AlipayExtConfig getAlipayExtConfig(PayConfig config) {
        if (config == null || config.getExtConfig() == null || config.getExtConfig().isEmpty()) {
            return null;
        }
        try {
            return JSONUtil.toBean(config.getExtConfig(), AlipayExtConfig.class);
        } catch (Exception e) {
            log.error("解析支付宝扩展配置失败", e);
            return null;
        }
    }

    @Override
    public void initStrategy(PayConfig config) {
        log.info("初始化支付宝APP支付配置, appId: {}", config.getAppId());
        this.currentConfig = config;
        this.extConfig = getAlipayExtConfig(config);

        if (config.getAppId() == null) {
            throw new BizException(PayResultCode.ALIPAY_CONFIG_INCOMPLETE);
        }
        if (extConfig == null || extConfig.getAppPrivateKey() == null || extConfig.getAlipayPublicKey() == null) {
            throw new BizException(PayResultCode.ALIPAY_CONFIG_INCOMPLETE);
        }

        try {
            AliPayApiConfig aliPayApiConfig = AliPayApiConfig.builder()
                    .setAppId(config.getAppId())
                    .setPrivateKey(extConfig.getAppPrivateKey())
                    .setAliPayPublicKey(extConfig.getAlipayPublicKey())
                    .setServiceUrl(extConfig.getServiceUrl())
                    .setCharset(extConfig.getCharset())
                    .setSignType(extConfig.getSignType())
                    .build();

            AliPayApiConfigKit.removeApiConfig(config.getAppId());
            AliPayApiConfigKit.putApiConfig(aliPayApiConfig);
            log.info("支付宝APP支付配置初始化成功");
        } catch (Exception e) {
            log.error("初始化支付宝配置失败", e);
            throw new BizException(PayResultCode.ALIPAY_INIT_FAILED, e.getMessage());
        }
    }

    @Override
    public PaymentResponseVO paymentRequest(PayPaymentChannel paymentRequest) {
        validatePayRecord(paymentRequest);
        logPayInfo("创建支付宝APP支付单", paymentRequest.getPaymentChannelNo());

        AlipayClient aliPayClient = AliPayApiConfigKit.getApiConfig(currentConfig.getAppId()).getAliPayClient();

        try {
            AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
            model.setOutTradeNo(paymentRequest.getPaymentChannelNo());
            model.setTotalAmount(String.format("%.2f", paymentRequest.getAmount()));
            model.setSubject(paymentRequest.getPaymentContent());
            model.setProductCode("QUICK_MSECURITY_PAY");
            model.setBody(paymentRequest.getPaymentContent());
            model.setPassbackParams(paymentRequest.getMemberId().toString());
            model.setTimeoutExpress(extConfig.getTimeoutExpress());

            String notifyUrl = currentConfig.getPaymentNotifyUrl();

            AlipayTradeAppPayResponse response = AliPayApi.appPayToResponse(aliPayClient, model, notifyUrl);
            if (!response.isSuccess()) {
                log.error("创建支付宝APP支付单失败: {}", response.getSubMsg());
                throw new BizException(PayResultCode.ALIPAY_APP_PAY_FAILED);
            }

            return PaymentResponseVO.builder()
                    .payNo(paymentRequest.getPaymentChannelNo())
                    .payParam(response.getBody())
                    .build();

        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建支付宝APP支付订单异常", e);
            throw new BizException(PayResultCode.ALIPAY_APP_PAY_FAILED);
        }
    }

    @Override
    public PaymentResultDTO paymentResult(HttpServletRequest request) {
        log.info("支付宝APP支付回调");

        try {
            Map<String, String> params = AliPayApi.toMap(request);
            log.info("支付宝回调参数: {}", JSONObject.toJSONString(params));

            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    extConfig.getAlipayPublicKey(),
                    extConfig.getCharset(),
                    extConfig.getSignType()
            );

            if (!signVerified) {
                log.error("支付宝APP支付回调验签失败");
                return new PaymentResultDTO()
                        .setStatus(PayStatusEnum.PAY_FAIL.getValue())
                        .setNotifyData(JSONObject.toJSONString(params));
            }

            String payNo = params.get("out_trade_no");
            String channelTradeNo = params.get("trade_no");
            PaymentResultDTO callbackDTO;
            if ("TRADE_SUCCESS".equals(params.get("trade_status"))) {
                callbackDTO = createSuccessCallback(payNo, channelTradeNo, JSONObject.toJSONString(params));
            } else {
                callbackDTO = createFailCallback(payNo, channelTradeNo, JSONObject.toJSONString(params));
            }

            return callbackDTO;
        } catch (Exception e) {
            log.info("支付宝APP支付回调异常", e);
            throw new BizException(PayResultCode.ALIPAY_CREATE_ORDER_FAILED);
        }
    }

    @Override
    public PaymentResponseVO queryStatus(String paymentChannelNo) {
        logPayInfo("查询支付宝APP支付单状态", paymentChannelNo);
        AlipayClient aliPayClient = AliPayApiConfigKit.getApiConfig(currentConfig.getAppId()).getAliPayClient();

        try {
            AlipayTradeQueryModel model = new AlipayTradeQueryModel();
            model.setOutTradeNo(paymentChannelNo);
            AlipayTradeQueryResponse response = AliPayApi.tradeQueryToResponse(aliPayClient, model, null);

            if (response.isSuccess()) {
                String tradeStatus = response.getTradeStatus();
                String channelTradeNo = response.getTradeNo();
                if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                    return PaymentResponseVO.builder()
                            .payNo(paymentChannelNo)
                            .channelTradeNo(channelTradeNo)
                            .payStatus(PayStatusEnum.PAY_SUCCESS.getValue())
                            .build();
                } else if ("WAIT_BUYER_PAY".equals(tradeStatus)) {
                    return PaymentResponseVO.builder()
                            .payNo(paymentChannelNo)
                            .payStatus(PayStatusEnum.PAY_SUCCESS.getValue())
                            .build();
                } else {
                    return PaymentResponseVO.builder()
                            .payNo(paymentChannelNo)
                            .payStatus(PayStatusEnum.PAY_SUCCESS.getValue())
                            .build();
                }
            }

            log.error("支付宝查询订单失败，订单号：{}，错误码：{}, 错误信息: {}",
                    paymentChannelNo, response.getSubCode(), response.getSubMsg());

            return PaymentResponseVO.builder()
                    .payNo(paymentChannelNo)
                    .payStatus(PayStatusEnum.PAY_SUCCESS.getValue())
                    .build();

        } catch (Exception e) {
            log.error("查询支付宝APP订单状态异常", e);
            throw new BizException(PayResultCode.ALIPAY_APP_PAY_FAILED);
        }
    }

    @Override
    public boolean closePayment(String paymentChannelNo) {
        logPayInfo("关闭支付宝APP支付单", paymentChannelNo);
        AlipayClient aliPayClient = AliPayApiConfigKit.getApiConfig(currentConfig.getAppId()).getAliPayClient();

        try {
            AlipayTradeCloseModel model = new AlipayTradeCloseModel();
            model.setOutTradeNo(paymentChannelNo);
            AlipayTradeCloseResponse response = AliPayApi.tradeCloseToResponse(aliPayClient, model, null);

            if (response.isSuccess()) {
                log.info("支付宝APP订单关闭成功，订单号: {}", paymentChannelNo);
                return true;
            } else {
                log.error("关闭支付宝APP订单失败，订单号：{}，错误码：{}, 错误信息: {}",
                        paymentChannelNo, response.getSubCode(), response.getSubMsg());
                return false;
            }
        } catch (Exception e) {
            log.error("关闭支付宝APP支付单异常", e);
            return false;
        }
    }

    @Override
    public boolean refund(PayRefundChannel refundRequest) {
        logPayInfo("申请支付宝APP退款", refundRequest.getPaymentChannelNo());
        AlipayClient aliPayClient = AliPayApiConfigKit.getApiConfig(currentConfig.getAppId()).getAliPayClient();

        try {
            AlipayTradeRefundModel model = new AlipayTradeRefundModel();
            model.setOutTradeNo(refundRequest.getPaymentChannelNo());
            model.setRefundAmount(String.format("%.2f", refundRequest.getAmount()));
            model.setOutRequestNo(refundRequest.getRefundChannelNo());

            String notifyUrl = currentConfig.getRefundNotifyUrl();
            AlipayTradeRefundResponse response = AliPayApi.tradeRefundToResponse(aliPayClient, model, notifyUrl);

            if (!response.isSuccess()) {
                log.error("支付宝APP退款失败，订单号：{}，错误码：{}，错误信息：{}",
                        refundRequest.getPaymentChannelNo(), response.getSubCode(), response.getSubMsg());
                return false;
            }

            refundRequest.setChannelTradeNo(response.getTradeNo());
            return true;
        } catch (Exception e) {
            log.error("申请支付宝APP退款异常", e);
            return false;
        }
    }

    @Override
    public RefundResultDTO refundCallback(HttpServletRequest request) {
        log.info("支付宝APP退款回调");

        try {
            Map<String, String> params = AliPayApi.toMap(request);
            log.info("支付宝退款回调参数: {}", JSONObject.toJSONString(params));

            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    extConfig.getAlipayPublicKey(),
                    extConfig.getCharset(),
                    extConfig.getSignType()
            );

            if (!signVerified) {
                log.error("支付宝退款回调验签失败");
                return null;
            }

            String tradeStatus = params.get("trade_status");
            if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
                log.warn("支付宝退款回调状态非成功: {}", tradeStatus);
                return null;
            }

            String outRequestNo = params.get("out_request_no");
            String tradeNo = params.get("trade_no");
            String refundFee = params.get("refund_fee");
            String gmtRefund = params.get("gmt_refund");

            BigDecimal refundAmount = BigDecimal.ZERO;
            if (StrUtil.isNotBlank(refundFee)) {
                refundAmount = new BigDecimal(refundFee);
            }

            return new RefundResultDTO()
                    .setRefundNo(outRequestNo)
                    .setChannelTradeNo(tradeNo)
                    .setStatus(RefundStatusEnum.REFUND_SUCCESS.getValue())
                    .setRefundAmount(refundAmount)
                    .setRefundTime(gmtRefund)
                    .setNotifyData(JSONObject.toJSONString(params));
        } catch (Exception e) {
            log.error("处理支付宝APP退款回调异常", e);
            return null;
        }
    }

    @Override
    public String getPayChannel() {
        return PayChannelEnum.ALIPAY_APP.getValue();
    }
}
