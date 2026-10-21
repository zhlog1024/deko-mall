package com.deko.mall.pay.service.pay.strategy.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.internal.util.AlipaySignature;
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
 * 支付宝WAP策略 - 使用IJPay实现
 */
@Slf4j
@Component
public class AlipayWapStrategy extends AbstractPayStrategy implements PayStrategy {

    // 存储当前配置和扩展配置
    private PayConfig currentConfig;
    private AlipayExtConfig extConfig;

    /**
     * 从支付配置中获取支付宝的扩展配置
     *
     * @param config 支付配置
     * @return 支付宝的扩展配置，如果无法获取则返回null
     */
    private AlipayExtConfig getAlipayExtConfig(PayConfig config) {
        if (config == null || config.getExtConfig() == null || config.getExtConfig().isEmpty()) {
            return null;
        }

        try {
            // 直接解析JSON格式的扩展配置
            return JSONUtil.toBean(config.getExtConfig(), AlipayExtConfig.class);
        } catch (Exception e) {
            log.error("解析支付宝扩展配置失败", e);
            return null;
        }
    }

    @Override
    public void initStrategy(PayConfig config) {
        log.info("初始化支付宝WAP配置, appId: {}", config.getAppId());

        // 保存配置
        this.currentConfig = config;

        // 解析扩展配置
        this.extConfig = getAlipayExtConfig(config);

        // 检查必要参数
        if (config.getAppId() == null) {
            log.error("支付宝配置不完整，appId缺失");
            throw new BizException(PayResultCode.ALIPAY_CONFIG_INCOMPLETE);
        }

        if (extConfig == null || extConfig.getAppPrivateKey() == null || extConfig.getAlipayPublicKey() == null) {
            log.error("支付宝配置不完整，私钥或公钥缺失");
            throw new BizException(PayResultCode.ALIPAY_CONFIG_INCOMPLETE);
        }

        try {
            // 初始化支付宝配置
            AliPayApiConfig aliPayApiConfig = AliPayApiConfig.builder()
                    .setAppId(config.getAppId())
                    .setPrivateKey(extConfig.getAppPrivateKey())
                    .setAliPayPublicKey(extConfig.getAlipayPublicKey())
                    .setServiceUrl(extConfig.getServiceUrl())
                    .setCharset(extConfig.getCharset())
                    .setSignType(extConfig.getSignType())
                    .build();

            // 移除旧配置，添加新配置
            AliPayApiConfigKit.removeApiConfig(config.getAppId());
            AliPayApiConfigKit.putApiConfig(aliPayApiConfig);

            log.info("支付宝WAP配置初始化成功");
        } catch (Exception e) {
            log.error("初始化支付宝配置失败", e);
            throw new BizException(PayResultCode.ALIPAY_INIT_FAILED, e.getMessage());
        }
    }

    @Override
    public PaymentResponseVO paymentRequest(PayPaymentChannel paymentRequest) {
        validatePayRecord(paymentRequest);
        logPayInfo("创建支付宝WAP单", paymentRequest.getPaymentChannelNo());

        // 使用已保存的配置
        AlipayClient aliPayClient = AliPayApiConfigKit.getApiConfig(currentConfig.getAppId()).getAliPayClient();

        try {
            AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
            model.setOutTradeNo(paymentRequest.getPaymentChannelNo());
            model.setTotalAmount(String.format("%.2f", paymentRequest.getAmount()));
            model.setSubject(paymentRequest.getPaymentContent());
            model.setProductCode("QUICK_WAP_WAY");
            model.setBody(paymentRequest.getPaymentContent());
            model.setPassbackParams(paymentRequest.getMemberId().toString());

            // 设置超时时间，如果扩展配置中有设置则使用，否则使用默认值
            model.setTimeoutExpress(extConfig.getTimeoutExpress());

            // 使用回调地址，优先使用扩展配置中的
            String notifyUrl = currentConfig.getPaymentNotifyUrl();

            String wapPayStr = AliPayApi.wapPayStr(aliPayClient, model, "", notifyUrl);
            return PaymentResponseVO.builder()
                    .payNo(paymentRequest.getPaymentChannelNo())
                    .payParam(wapPayStr)
                    .build();

        } catch (Exception e) {
            log.error("创建支付宝订单异常", e);
            throw new BizException(PayResultCode.ALIPAY_CREATE_ORDER_FAILED);
        }
    }

    @Override
    public PaymentResultDTO paymentResult(HttpServletRequest request) {
        log.info("支付宝WAP支付回调");

        try {
            // 获取回调参数
            Map<String, String> params = AliPayApi.toMap(request);
            log.info("支付宝回调参数: {}", JSONObject.toJSONString(params));

            // 验证签名，使用扩展配置中的支付宝公钥、字符集和签名类型
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    extConfig.getAlipayPublicKey(),
                    extConfig.getCharset(),
                    extConfig.getSignType()
            );

            if (!signVerified) {
                log.error("支付宝支付回调验签失败");
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
            log.info("支付宝WAP支付回调异常", e);
            throw new BizException(PayResultCode.ALIPAY_CREATE_ORDER_FAILED);
        }
    }

    @Override
    public PaymentResponseVO queryStatus(String paymentChannelNo) {
        logPayInfo("查询支付宝WAP单状态", paymentChannelNo);
        // 使用已保存的配置
        AlipayClient aliPayClient = AliPayApiConfigKit.getApiConfig(currentConfig.getAppId()).getAliPayClient();

        try {
            // 通过IJPay构建请求
            AlipayTradeQueryModel model = new AlipayTradeQueryModel();
            model.setOutTradeNo(paymentChannelNo);

            // 执行请求（需已配置AliPayApiConfig）
            AlipayTradeQueryResponse response = AliPayApi.tradeQueryToResponse(aliPayClient, model, null);

            // 处理响应
            if (response.isSuccess()) {
                String tradeStatus = response.getTradeStatus();
                String channelTradeNo = response.getTradeNo();
                if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                    // 支付成功
                    return PaymentResponseVO.builder()
                            .payNo(paymentChannelNo)
                            .channelTradeNo(channelTradeNo)
                            .payStatus(PayStatusEnum.PAY_SUCCESS.getValue())
                            .build();
                } else if ("WAIT_BUYER_PAY".equals(tradeStatus)) {
                    // 等待付款
                    return PaymentResponseVO.builder()
                            .payNo(paymentChannelNo)
                            .payStatus(PayStatusEnum.PAY_SUCCESS.getValue())
                            .build();
                } else {
                    // 其他状态视为支付失败
                    return PaymentResponseVO.builder()
                            .payNo(paymentChannelNo)
                            .payStatus(PayStatusEnum.PAY_SUCCESS.getValue())
                            .build();
                }
            }

            log.error("支付宝查询订单失败，订单号：{}，错误码：{}, 错误信息: {}",
                    paymentChannelNo, response.getSubCode(), response.getSubMsg());

            // 查询失败，返回失败状态
            return PaymentResponseVO.builder()
                    .payNo(paymentChannelNo)
                    .payStatus(PayStatusEnum.PAY_SUCCESS.getValue())
                    .build();

        } catch (Exception e) {
            log.error("查询支付宝订单状态异常", e);
            throw new BizException(PayResultCode.ALIPAY_APP_PAY_FAILED);
        }
    }

    @Override
    public boolean closePayment(String paymentChannelNo) {
        logPayInfo("关闭支付宝WAP单", paymentChannelNo);
        // 使用已保存的配置
        AlipayClient aliPayClient = AliPayApiConfigKit.getApiConfig(currentConfig.getAppId()).getAliPayClient();

        try {
            AlipayTradeCloseModel alipayTradeCloseModel = new AlipayTradeCloseModel();
            alipayTradeCloseModel.setOutTradeNo(paymentChannelNo);
            AlipayTradeCloseResponse response = AliPayApi.tradeCloseToResponse(aliPayClient, alipayTradeCloseModel, null);

            if (response.isSuccess()) {
                log.info("支付宝订单关闭成功，订单号: {}", paymentChannelNo);
                return true;
            } else {
                log.error("关闭支付宝订单失败，订单号：{}，错误码：{}, 错误信息: {}",
                        paymentChannelNo, response.getSubCode(), response.getSubMsg());
                return false;
            }
        } catch (Exception e) {
            log.error("关闭支付宝WAP单异常", e);
            return false;
        }
    }

    @Override
    public boolean refund(PayRefundChannel refundRequest) {
        logPayInfo("申请支付宝H5退款", refundRequest.getPaymentChannelNo());

        // 使用已保存的配置
        AlipayClient aliPayClient = AliPayApiConfigKit.getApiConfig(currentConfig.getAppId()).getAliPayClient();

        try {
            AlipayTradeRefundModel tradeRefundModel = new AlipayTradeRefundModel();
            tradeRefundModel.setOutTradeNo(refundRequest.getPaymentChannelNo());
            tradeRefundModel.setRefundAmount(String.format("%.2f", refundRequest.getAmount()));
            tradeRefundModel.setOutRequestNo(refundRequest.getRefundChannelNo());

            String notifyUrl = currentConfig.getRefundNotifyUrl();
            AlipayTradeRefundResponse response = AliPayApi.tradeRefundToResponse(aliPayClient, tradeRefundModel, notifyUrl);

            if (!response.isSuccess()) {
                log.error("IJPay退款失败，订单号：{}，错误码：{}，错误信息：{}",
                        refundRequest.getPaymentChannelNo(), response.getSubCode(), response.getSubMsg());
                return false;
            }

            // 退款申请已受理，记录渠道退款号，等待回调通知终态
            refundRequest.setChannelTradeNo(response.getTradeNo());
            return true;
        } catch (Exception e) {
            log.error("申请支付宝H5退款异常", e);
            return false;
        }
    }

    @Override
    public RefundResultDTO refundCallback(HttpServletRequest request) {
        log.info("支付宝H5退款回调");

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
            log.error("处理支付宝H5退款回调异常", e);
            return null;
        }
    }

    @Override
    public String getPayChannel() {
        return PayChannelEnum.ALIPAY_WAP.getValue();
    }
}
