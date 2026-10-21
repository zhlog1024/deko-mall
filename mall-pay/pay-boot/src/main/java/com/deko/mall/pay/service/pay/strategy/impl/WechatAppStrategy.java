package com.deko.mall.pay.service.pay.strategy.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.PemUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.ijpay.core.IJPayHttpResponse;
import com.ijpay.core.enums.RequestMethodEnum;
import com.ijpay.core.kit.AesUtil;
import com.ijpay.core.kit.WxPayKit;
import com.ijpay.core.utils.DateTimeZoneUtil;
import com.ijpay.wxpay.WxPayApi;
import com.ijpay.wxpay.enums.WxDomainEnum;
import com.ijpay.wxpay.enums.v3.BasePayApiEnum;
import com.ijpay.wxpay.model.v3.Amount;
import com.ijpay.wxpay.model.v3.UnifiedOrderModel;
import com.deko.mall.common.web.exception.BizException;
import com.deko.mall.pay.api.enums.PayChannelEnum;
import com.deko.mall.pay.api.enums.PayStatusEnum;
import com.deko.mall.pay.api.enums.RefundStatusEnum;
import com.deko.mall.pay.api.dto.PaymentResultDTO;
import com.deko.mall.pay.api.dto.RefundResultDTO;
import com.deko.mall.pay.entity.PayConfig;
import com.deko.mall.pay.entity.PayPaymentChannel;
import com.deko.mall.pay.entity.PayRefundChannel;
import com.deko.mall.pay.entity.WechatPayExtConfig;
import com.deko.mall.pay.model.vo.PaymentResponseVO;
import com.deko.mall.pay.service.pay.strategy.PayStrategy;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Map;
import com.deko.mall.pay.config.PayResultCode;

/**
 * 微信APP支付策略 - 使用IJPay V3实现
 */
@Slf4j
@Component
public class WechatAppStrategy extends AbstractPayStrategy implements PayStrategy {

    private PayConfig currentConfig;

    @Override
    public void initStrategy(PayConfig config) {
        log.info("初始化微信APP支付V3配置, appId: {}, mchId: {}", config.getAppId(), config.getMchId());
        this.currentConfig = config;

        WechatPayExtConfig extConfig = getWechatExtConfig(config);
        currentConfig.setParsedExtConfig(extConfig);

        if (config.getAppId() == null || config.getMchId() == null) {
            throw new BizException(PayResultCode.WX_CONFIG_INCOMPLETE);
        }
        if (extConfig == null) {
            throw new BizException(PayResultCode.PAY_CONFIG_INCOMPLETE);
        }
        if (extConfig.getApiV3Key() == null) {
            throw new BizException(PayResultCode.WX_API_KEY_MISSING);
        }
        if (extConfig.getMerchantPrivateKey() == null) {
            throw new BizException(PayResultCode.WX_PRIVATE_KEY_MISSING);
        }
        if (extConfig.getMerchantSerialNo() == null) {
            throw new BizException(PayResultCode.WX_CERT_SERIAL_MISSING);
        }

        try {
            PrivateKey privateKey = PemUtil.readPemPrivateKey(
                    new ByteArrayInputStream(extConfig.getMerchantPrivateKey().getBytes()));
            extConfig.setParsedMerchantPrivateKey(privateKey);

            if (extConfig.getPlatformCert() != null) {
                try {
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    X509Certificate platformCert = (X509Certificate) cf.generateCertificate(
                            new ByteArrayInputStream(extConfig.getPlatformCert().getBytes()));
                    extConfig.setParsedPlatformCert(platformCert);
                } catch (Exception e) {
                    log.error("平台证书解析失败", e);
                }
            }
        } catch (Exception e) {
            log.error("证书解析失败", e);
            throw new BizException(PayResultCode.WX_CERT_FORMAT_ERROR);
        }
        log.info("微信APP支付配置初始化成功");
    }

    private WechatPayExtConfig getWechatExtConfig(PayConfig config) {
        if (config == null || config.getExtConfig() == null || config.getExtConfig().isEmpty()) {
            return null;
        }
        try {
            return JSONUtil.toBean(config.getExtConfig(), WechatPayExtConfig.class);
        } catch (Exception e) {
            log.error("解析微信支付扩展配置失败", e);
            return null;
        }
    }

    private WechatPayExtConfig getExtConfig() {
        if (currentConfig == null || currentConfig.getParsedExtConfig() == null) {
            throw new BizException(PayResultCode.PAY_CONFIG_INCOMPLETE);
        }
        return (WechatPayExtConfig) currentConfig.getParsedExtConfig();
    }

    @Override
    public PaymentResponseVO paymentRequest(PayPaymentChannel paymentRequest) {
        validatePayRecord(paymentRequest);
        logPayInfo("创建微信APP支付单", paymentRequest.getPaymentChannelNo());

        try {
            if (currentConfig == null) {
                throw new BizException(PayResultCode.WX_CONFIG_NOT_FOUND);
            }

            WechatPayExtConfig extConfig = getExtConfig();
            String appId = currentConfig.getAppId();
            String mchId = currentConfig.getMchId();
            String serialNo = extConfig.getMerchantSerialNo();
            PrivateKey privateKey = extConfig.getParsedMerchantPrivateKey();
            String timeExpire = DateTimeZoneUtil.dateToTimeZone(System.currentTimeMillis() + extConfig.getTimeoutExpress());

            // 构建统一下单参数（APP支付无需Payer/openid）
            UnifiedOrderModel unifiedOrderModel = new UnifiedOrderModel()
                    .setAppid(appId)
                    .setMchid(mchId)
                    .setDescription(paymentRequest.getPaymentContent())
                    .setOut_trade_no(paymentRequest.getPaymentChannelNo())
                    .setTime_expire(timeExpire)
                    .setAttach("商城订单支付")
                    .setNotify_url(currentConfig.getPaymentNotifyUrl());

            int amountInCents = paymentRequest.getAmount()
                    .multiply(BigDecimal.valueOf(100)).intValue();
            unifiedOrderModel.setAmount(new Amount().setTotal(amountInCents));

            log.info("微信APP统一下单参数: {}", JSONUtil.toJsonStr(unifiedOrderModel));

            IJPayHttpResponse response = WxPayApi.v3(
                    RequestMethodEnum.POST,
                    WxDomainEnum.CHINA.toString(),
                    BasePayApiEnum.APP_PAY.toString(),
                    mchId,
                    serialNo,
                    null,
                    privateKey,
                    JSONUtil.toJsonStr(unifiedOrderModel)
            );

            log.info("微信APP统一下单响应状态码: {}", response.getStatus());

            if (response.getStatus() == 200) {
                String body = response.getBody();
                JSONObject jsonObject = JSONUtil.parseObj(body);
                String prepayId = jsonObject.getStr("prepay_id");

                if (StrUtil.isEmpty(prepayId)) {
                    log.error("获取prepay_id失败: {}", body);
                    throw new BizException(PayResultCode.WX_PREPAY_ID_EMPTY);
                }

                // 生成APP调起支付参数
                Map<String, String> appParams = WxPayKit.appCreateSign(appId, mchId, prepayId, privateKey);

                log.info("生成APP支付参数: {}", JSONUtil.toJsonStr(appParams));

                return PaymentResponseVO.builder()
                        .payNo(paymentRequest.getPaymentChannelNo())
                        .mchId(mchId)
                        .payParam(JSON.toJSONString(appParams))
                        .build();
            } else {
                String errMsg = "创建微信APP支付单失败，状态码：" + response.getStatus();
                if (response.getStatus() != 200 && !StrUtil.isEmpty(response.getBody())) {
                    JSONObject jsonObject = JSONUtil.parseObj(response.getBody());
                    errMsg += "，错误信息：" + jsonObject.getStr("message");
                }
                log.error(errMsg);
                throw new BizException(errMsg);
            }
        } catch (Exception e) {
            log.error("创建微信APP支付单异常", e);
            throw new BizException(PayResultCode.WX_CREATE_ORDER_FAILED);
        }
    }

    @Override
    public PaymentResultDTO paymentResult(HttpServletRequest request) {
        try {
            String body = getRequestBody(request);
            log.info("微信APP支付V3回调通知数据: {}", body);

            String timestamp = request.getHeader("Wechatpay-Timestamp");
            String nonce = request.getHeader("Wechatpay-Nonce");
            String signature = request.getHeader("Wechatpay-Signature");
            String serialNo = request.getHeader("Wechatpay-Serial");

            WechatPayExtConfig extConfig = getExtConfig();
            if (currentConfig == null) {
                return createFailCallback(null, null, body);
            }
            if (extConfig.getParsedPlatformCert() == null) {
                return createFailCallback(null, null, "未找到平台证书");
            }

            boolean signatureValid = verifySignature(body, timestamp, nonce, signature, extConfig.getParsedPlatformCert());
            if (!signatureValid) {
                log.error("微信APP支付回调签名验证失败");
                return createFailCallback(null, null, "签名验证失败");
            }

            JSONObject jsonObject = JSONUtil.parseObj(body);
            String eventType = jsonObject.getStr("event_type");
            if (!"TRANSACTION.SUCCESS".equals(eventType)) {
                log.error("非支付成功回调，eventType: {}", eventType);
                return createFailCallback(null, null, body);
            }

            JSONObject resourceObj = jsonObject.getJSONObject("resource");
            String ciphertext = resourceObj.getStr("ciphertext");
            String associatedData = resourceObj.getStr("associated_data");
            String resourceNonce = resourceObj.getStr("nonce");

            log.info("解密参数：associatedData={}, resourceNonce={}, ciphertext长度={}",
                    associatedData, resourceNonce, ciphertext.length());

            AesUtil aesUtil = new AesUtil(extConfig.getApiV3Key().getBytes(StandardCharsets.UTF_8));
            String decryptData = aesUtil.decryptToString(
                    associatedData != null ? associatedData.getBytes(StandardCharsets.UTF_8) : null,
                    resourceNonce.getBytes(StandardCharsets.UTF_8),
                    ciphertext
            );
            log.info("解密后的支付结果通知: {}", decryptData);

            JSONObject result = JSONUtil.parseObj(decryptData);
            String tradeState = result.getStr("trade_state");
            String payNo = result.getStr("out_trade_no");
            String transactionId = result.getStr("transaction_id");

            if ("SUCCESS".equals(tradeState)) {
                return createSuccessCallback(payNo, transactionId, body);
            } else {
                return createFailCallback(payNo, transactionId, body);
            }
        } catch (Exception e) {
            log.error("处理微信APP支付回调异常", e);
            return createFailCallback(null, null, "处理失败: " + e.getMessage());
        }
    }

    private String getRequestBody(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            log.error("读取请求体异常", e);
            throw new BizException(PayResultCode.PAY_BODY_READ_ERROR);
        }
        return sb.toString();
    }

    @Override
    public PaymentResponseVO queryStatus(String paymentChannelNo) {
        logPayInfo("查询微信APP支付状态V3", paymentChannelNo);
        try {
            if (currentConfig == null) {
                throw new BizException(PayResultCode.WX_CONFIG_NOT_FOUND);
            }
            WechatPayExtConfig extConfig = getExtConfig();
            String mchId = currentConfig.getMchId();
            String serialNo = extConfig.getMerchantSerialNo();

            IJPayHttpResponse response = WxPayApi.v3(
                    RequestMethodEnum.GET,
                    WxDomainEnum.CHINA.toString(),
                    String.format(BasePayApiEnum.ORDER_QUERY_BY_OUT_TRADE_NO.toString(), paymentChannelNo),
                    mchId,
                    serialNo,
                    null,
                    extConfig.getParsedMerchantPrivateKey(),
                    ""
            );

            if (response.getStatus() == 200) {
                String body = response.getBody();
                JSONObject result = JSONUtil.parseObj(body);
                String tradeState = result.getStr("trade_state");
                String channelTradeNo = result.getStr("transaction_id");

                if ("SUCCESS".equals(tradeState)) {
                    return PaymentResponseVO.builder()
                            .payNo(paymentChannelNo)
                            .mchId(mchId)
                            .channelTradeNo(channelTradeNo)
                            .payStatus(PayStatusEnum.PAY_SUCCESS.getValue())
                            .build();
                } else if ("NOTPAY".equals(tradeState)) {
                    return PaymentResponseVO.builder()
                            .payNo(paymentChannelNo)
                            .mchId(mchId)
                            .payStatus(PayStatusEnum.PAY_FAIL.getValue())
                            .build();
                } else {
                    return PaymentResponseVO.builder()
                            .payNo(paymentChannelNo)
                            .mchId(mchId)
                            .payStatus(PayStatusEnum.PAY_FAIL.getValue())
                            .build();
                }
            } else {
                throw new BizException(PayResultCode.PAY_QUERY_STATUS_ERROR, String.valueOf(response.getStatus()));
            }
        } catch (Exception e) {
            log.error("查询微信APP支付状态异常", e);
            throw new BizException(PayResultCode.PAY_QUERY_STATUS_ERROR);
        }
    }

    @Override
    public boolean closePayment(String paymentChannelNo) {
        logPayInfo("关闭微信APP支付单", paymentChannelNo);
        try {
            if (currentConfig == null) {
                throw new BizException(PayResultCode.WX_CONFIG_NOT_FOUND);
            }
            WechatPayExtConfig extConfig = getExtConfig();
            String mchId = currentConfig.getMchId();
            String serialNo = extConfig.getMerchantSerialNo();

            JSONObject params = new JSONObject();
            params.set("mchid", mchId);

            IJPayHttpResponse response = WxPayApi.v3(
                    RequestMethodEnum.POST,
                    WxDomainEnum.CHINA.toString(),
                    String.format(BasePayApiEnum.CLOSE_ORDER_BY_OUT_TRADE_NO.toString(), paymentChannelNo),
                    mchId,
                    serialNo,
                    null,
                    extConfig.getParsedMerchantPrivateKey(),
                    params.toString()
            );
            return response.getStatus() == 204;
        } catch (Exception e) {
            log.error("关闭微信APP支付单异常", e);
            return false;
        }
    }

    @Override
    public boolean refund(PayRefundChannel refundRequest) {
        logPayInfo("申请微信APP退款V3", refundRequest.getPaymentChannelNo());
        try {
            if (currentConfig == null) {
                throw new BizException(PayResultCode.WX_CONFIG_NOT_FOUND);
            }
            WechatPayExtConfig extConfig = getExtConfig();
            String mchId = currentConfig.getMchId();
            String serialNo = extConfig.getMerchantSerialNo();

            JSONObject params = new JSONObject();
            params.set("out_trade_no", refundRequest.getPaymentChannelNo());
            params.set("out_refund_no", refundRequest.getRefundChannelNo());

            int refundAmountInCents = refundRequest.getAmount()
                    .multiply(BigDecimal.valueOf(100)).intValue();
            JSONObject amount = new JSONObject();
            amount.set("refund", refundAmountInCents);
            amount.set("total", refundAmountInCents);
            amount.set("currency", "CNY");
            params.set("amount", amount);

            if (StrUtil.isNotEmpty(currentConfig.getRefundNotifyUrl())) {
                params.set("notify_url", currentConfig.getRefundNotifyUrl());
            }

            IJPayHttpResponse response = WxPayApi.v3(
                    RequestMethodEnum.POST,
                    WxDomainEnum.CHINA.toString(),
                    BasePayApiEnum.REFUND.toString(),
                    mchId,
                    serialNo,
                    null,
                    extConfig.getParsedMerchantPrivateKey(),
                    params.toString()
            );

            if (response.getStatus() == 200) {
                String body = response.getBody();
                JSONObject result = JSONUtil.parseObj(body);
                String refundId = result.getStr("refund_id");
                refundRequest.setChannelTradeNo(refundId);
                String status = result.getStr("status");
                return "SUCCESS".equals(status) || "PROCESSING".equals(status);
            } else {
                log.error("申请退款失败，状态码: {}, 响应: {}", response.getStatus(), response.getBody());
                return false;
            }
        } catch (Exception e) {
            log.error("申请微信APP退款异常", e);
            return false;
        }
    }

    @Override
    public RefundResultDTO refundCallback(HttpServletRequest request) {
        try {
            String body = getRequestBody(request);
            log.info("微信APP退款回调开始，请求数据：{}", body);

            // V3 回调：JSON + AES 解密
            JSONObject jsonObject = JSONUtil.parseObj(body);
            String eventType = jsonObject.getStr("event_type");

            if (!"REFUND.SUCCESS".equals(eventType)) {
                log.warn("非退款成功回调，eventType: {}", eventType);
                return null;
            }

            WechatPayExtConfig extConfig = getExtConfig();
            JSONObject resourceObj = jsonObject.getJSONObject("resource");
            String ciphertext = resourceObj.getStr("ciphertext");
            String associatedData = resourceObj.getStr("associated_data");
            String resourceNonce = resourceObj.getStr("nonce");

            AesUtil aesUtil = new AesUtil(extConfig.getApiV3Key().getBytes(StandardCharsets.UTF_8));
            String decryptData = aesUtil.decryptToString(
                    associatedData != null ? associatedData.getBytes(StandardCharsets.UTF_8) : null,
                    resourceNonce.getBytes(StandardCharsets.UTF_8),
                    ciphertext
            );
            log.info("解密后的退款结果通知: {}", decryptData);

            JSONObject result = JSONUtil.parseObj(decryptData);
            String refundStatus = result.getStr("refund_status");

            if ("SUCCESS".equals(refundStatus)) {
                JSONObject amountObj = result.getJSONObject("amount");
                BigDecimal refundAmount = BigDecimal.ZERO;
                if (amountObj != null) {
                    BigDecimal refundFeeInCents = amountObj.getBigDecimal("refund");
                    if (refundFeeInCents != null) {
                        refundAmount = refundFeeInCents.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    }
                }
                return new RefundResultDTO()
                        .setRefundNo(result.getStr("out_refund_no"))
                        .setChannelTradeNo(result.getStr("refund_id"))
                        .setStatus(RefundStatusEnum.REFUND_SUCCESS.getValue())
                        .setRefundTime(result.getStr("success_time"))
                        .setRefundAmount(refundAmount)
                        .setNotifyData(decryptData);
            }
            log.error("处理微信APP退款回调失败，状态异常: refundStatus={}", refundStatus);
            return null;
        } catch (Exception e) {
            log.error("处理微信APP退款回调异常", e);
            return null;
        }
    }

    private boolean verifySignature(String body, String timestamp, String nonce, String signature, X509Certificate certificate) {
        try {
            if (StrUtil.isEmpty(body) || StrUtil.isEmpty(signature) || certificate == null) {
                return false;
            }
            String message = timestamp + "\n" + nonce + "\n" + body + "\n";
            java.security.Signature sign = java.security.Signature.getInstance("SHA256withRSA");
            sign.initVerify(certificate.getPublicKey());
            sign.update(message.getBytes(StandardCharsets.UTF_8));
            return sign.verify(java.util.Base64.getDecoder().decode(signature));
        } catch (Exception e) {
            log.error("验证签名异常", e);
            return false;
        }
    }

    @Override
    public String getPayChannel() {
        return PayChannelEnum.WECHAT_APP.getValue();
    }
}
