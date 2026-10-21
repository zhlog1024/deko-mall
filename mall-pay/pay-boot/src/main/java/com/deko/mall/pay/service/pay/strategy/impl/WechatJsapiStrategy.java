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
import com.ijpay.wxpay.model.v3.Payer;
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
import lombok.RequiredArgsConstructor;
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
 * 微信小程序支付策略 - 使用IJPay V3实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WechatJsapiStrategy extends AbstractPayStrategy implements PayStrategy {


    // 存储当前配置
    private PayConfig currentConfig;

    @Override
    public void initStrategy(PayConfig config) {
        log.info("初始化微信支付V3配置, appId: {}, mchId: {}", config.getAppId(), config.getMchId());

        // 保存配置
        this.currentConfig = config;

        // 解析扩展配置并保存到currentConfig中
        WechatPayExtConfig extConfig = getWechatExtConfig(config);
        currentConfig.setParsedExtConfig(extConfig);

        // 检查必要参数
        if (config.getAppId() == null || config.getMchId() == null) {
            log.error("微信支付配置不完整，appId: {}, mchId: {} 缺失", config.getAppId(), config.getMchId());
            throw new BizException(PayResultCode.WX_CONFIG_INCOMPLETE);
        }

        // 检查扩展配置是否完整
        if (extConfig == null) {
            log.error("微信支付扩展配置为空");
            throw new BizException(PayResultCode.PAY_CONFIG_INCOMPLETE);
        }

        // 验证必须的扩展配置参数
        if (extConfig.getApiV3Key() == null) {
            log.error("微信支付扩展配置不完整，缺少API V3密钥");
            throw new BizException(PayResultCode.WX_API_KEY_MISSING);
        }

        if (extConfig.getMerchantPrivateKey() == null) {
            log.error("微信支付扩展配置不完整，缺少商户私钥");
            throw new BizException(PayResultCode.WX_PRIVATE_KEY_MISSING);
        }

        if (extConfig.getMerchantSerialNo() == null) {
            log.error("微信支付扩展配置不完整，缺少商户证书序列号");
            throw new BizException(PayResultCode.WX_CERT_SERIAL_MISSING);
        }


        try {
            // 解析商户私钥
            PrivateKey privateKey = PemUtil.readPemPrivateKey(
                    new ByteArrayInputStream(extConfig.getMerchantPrivateKey().getBytes())
            );
            extConfig.setParsedMerchantPrivateKey(privateKey);
            log.info("商户私钥解析成功");

            // 如果有平台证书，解析平台证书
            if (extConfig.getPlatformCert() != null) {
                // 注意: 根据实际情况调整证书解析方法
                // 如果PemUtil没有readPemCertificate方法，可以使用以下代码
                try {
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    X509Certificate platformCert = (X509Certificate) cf.generateCertificate(
                            new ByteArrayInputStream(extConfig.getPlatformCert().getBytes())
                    );
                    extConfig.setParsedPlatformCert(platformCert);
                    log.info("平台证书解析成功");
                } catch (Exception e) {
                    log.error("平台证书解析失败", e);
                }
            }
        } catch (Exception e) {
            log.error("证书解析失败", e);
            throw new BizException(PayResultCode.WX_CERT_FORMAT_ERROR);
        }

        log.info("微信支付配置初始化成功");
    }


    /**
     * 从支付配置中获取微信支付的扩展配置
     *
     * @param config 支付配置
     * @return 微信支付的扩展配置，如果无法获取则返回null
     */
    public WechatPayExtConfig getWechatExtConfig(PayConfig config) {
        if (config == null || config.getExtConfig() == null || config.getExtConfig().isEmpty()) {
            return null;
        }

        try {
            // 直接解析JSON格式的扩展配置
            return JSONUtil.toBean(config.getExtConfig(), WechatPayExtConfig.class);
        } catch (Exception e) {
            log.error("解析微信支付扩展配置失败", e);
            return null;
        }
    }

    /**
     * 获取当前微信扩展配置
     *
     * @return 微信扩展配置
     */
    private WechatPayExtConfig getExtConfig() {
        if (currentConfig == null || currentConfig.getParsedExtConfig() == null) {
            log.error("未找到微信支付扩展配置");
            throw new BizException(PayResultCode.PAY_CONFIG_INCOMPLETE);
        }
        return (WechatPayExtConfig) currentConfig.getParsedExtConfig();
    }

    @Override
    public PaymentResponseVO paymentRequest(PayPaymentChannel paymentRequest) {
        validatePayRecord(paymentRequest);
        logPayInfo("创建微信小程序支付单", paymentRequest.getPaymentChannelNo());

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

            // 构建统一下单请求参数
            UnifiedOrderModel unifiedOrderModel = new UnifiedOrderModel()
                    // 小程序APPID
                    .setAppid(appId)
                    // 商户号
                    .setMchid(mchId)
                    // 商品描述
                    .setDescription(paymentRequest.getPaymentContent())
                    // 商户订单号
                    .setOut_trade_no(paymentRequest.getPaymentChannelNo())
                    // 订单过期时间
                    .setTime_expire(timeExpire)
                    // 附加数据，在查询API和支付通知中原样返回
                    .setAttach("商城订单支付")
                    // 支付结果通知回调地址
                    .setNotify_url(currentConfig.getPaymentNotifyUrl());

            // 订单金额（元转换为分）
            BigDecimal amountInYuan = paymentRequest.getAmount();
            int amountInCents = amountInYuan.multiply(BigDecimal.valueOf(100)).intValue();
            unifiedOrderModel.setAmount(new Amount().setTotal(amountInCents));
            // 支付者信息
            unifiedOrderModel.setPayer(new Payer().setOpenid(paymentRequest.getChannelUserId()));

            log.info("统一下单参数: {}", JSONUtil.toJsonStr(unifiedOrderModel));

            // V3 JSAPI下单接口请求
            IJPayHttpResponse response = WxPayApi.v3(
                    RequestMethodEnum.POST,
                    WxDomainEnum.CHINA.toString(),
                    BasePayApiEnum.JS_API_PAY.toString(),
                    mchId,
                    serialNo,
                    null,
                    privateKey,
                    JSONUtil.toJsonStr(unifiedOrderModel)
            );

            log.info("统一下单响应状态码: {}", response.getStatus());

            // 验证响应
            if (response.getStatus() == 200) {
                String body = response.getBody();
                JSONObject jsonObject = JSONUtil.parseObj(body);
                String prepayId = jsonObject.getStr("prepay_id");

                if (StrUtil.isEmpty(prepayId)) {
                    log.error("获取prepay_id失败: {}", body);
                    throw new BizException(PayResultCode.WX_PREPAY_ID_EMPTY);
                }

                // 生成小程序调起支付API参数
                Map<String, String> jsapiParams = WxPayKit.jsApiCreateSign(
                        appId,
                        prepayId,
                        privateKey
                );

                log.info("生成小程序支付参数: {}", JSONUtil.toJsonStr(jsapiParams));

                return PaymentResponseVO.builder()
                        .payNo(paymentRequest.getPaymentChannelNo())
                        .mchId(mchId)
                        .payParam(JSON.toJSONString(jsapiParams))
                        .build();
            } else {
                String errMsg = "创建微信支付单失败，状态码：" + response.getStatus();
                if (response.getStatus() != 200 && !StrUtil.isEmpty(response.getBody())) {
                    JSONObject jsonObject = JSONUtil.parseObj(response.getBody());
                    errMsg += "，错误信息：" + jsonObject.getStr("message");
                }
                log.error(errMsg);
                throw new BizException(errMsg);
            }
        } catch (Exception e) {
            log.error("创建微信小程序支付单异常", e);
            throw new BizException(PayResultCode.WX_CREATE_ORDER_FAILED);
        }
    }

    @Override
    public PaymentResultDTO paymentResult(HttpServletRequest request) {
        try {
            // 读取通知数据
            String body = getRequestBody(request);
            log.info("微信支付V3回调通知数据: {}", body);

            // 获取请求头信息
            String timestamp = request.getHeader("Wechatpay-Timestamp");
            log.info("微信支付V3回调通知时间戳: {}", timestamp);
            String nonce = request.getHeader("Wechatpay-Nonce");
            log.info("微信支付V3回调通知随机数: {}", nonce);
            String signature = request.getHeader("Wechatpay-Signature");
            log.info("微信支付V3回调通知签名: {}", signature);
            String serialNo = request.getHeader("Wechatpay-Serial");
            log.info("微信支付V3回调通知证书序列号: {}", serialNo);

            // 获取配置
            WechatPayExtConfig extConfig = getExtConfig();
            if (currentConfig == null) {
                log.error("未找到微信支付配置");
                return createFailCallback(null, null, body);
            }

            // 验证签名
            if (extConfig.getParsedPlatformCert() == null) {
                log.error("未找到已解析的平台证书");
                return createFailCallback(null, null, "未找到平台证书");
            }

            boolean signatureValid = verifySignature(body, timestamp, nonce, signature, extConfig.getParsedPlatformCert());
            if (!signatureValid) {
                log.error("微信支付回调签名验证失败");
                return createFailCallback(null, null, "签名验证失败");
            }

            // 验证通过后解析回调数据
            JSONObject jsonObject = JSONUtil.parseObj(body);
            String eventType = jsonObject.getStr("event_type");

            if (!("TRANSACTION.SUCCESS".equals(eventType))) {
                log.error("非支付成功回调，eventType: {}", eventType);
                return createFailCallback(null, null, body);
            }

            // 获取并解密资源
            JSONObject resourceObj = jsonObject.getJSONObject("resource");
            String ciphertext = resourceObj.getStr("ciphertext");
            String associatedData = resourceObj.getStr("associated_data");
            String resourceNonce = resourceObj.getStr("nonce");
            // 解密回调数据
            String decryptData;

            log.info("解密参数：associatedData={}, resourceNonce={}, ciphertext长度={}",
                    associatedData, resourceNonce, ciphertext.length());

            try {
                AesUtil aesUtil = new AesUtil(extConfig.getApiV3Key().getBytes(StandardCharsets.UTF_8));
                // 使用 resource 中的 nonce 而不是响应头中的 nonce
                decryptData = aesUtil.decryptToString(
                        associatedData != null ? associatedData.getBytes(StandardCharsets.UTF_8) : null,
                        resourceNonce.getBytes(StandardCharsets.UTF_8),
                        ciphertext
                );
                log.info("解密后的支付结果通知: {}", decryptData);
            } catch (Exception e) {
                log.error("解密微信支付通知失败: {}", e.getMessage(), e);
                return createFailCallback(null, null, "解密失败: " + e.getMessage());
            }

            // 解析支付结果
            JSONObject result = JSONUtil.parseObj(decryptData);
            String tradeState = result.getStr("trade_state");
            String payNo = result.getStr("out_trade_no");
            String transactionId = result.getStr("transaction_id");

            if ("SUCCESS".equals(tradeState)) {
                // 处理支付成功的回调
                return createSuccessCallback(payNo, transactionId, body);
            } else {
                return createFailCallback(payNo, transactionId, body);
            }
        } catch (Exception e) {
            log.error("处理微信支付回调异常", e);
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
        logPayInfo("查询微信小程序支付状态V3", paymentChannelNo);

        try {
            if (currentConfig == null) {
                throw new BizException(PayResultCode.WX_CONFIG_NOT_FOUND);
            }

            WechatPayExtConfig extConfig = getExtConfig();
            String mchId = currentConfig.getMchId();
            String serialNo = extConfig.getMerchantSerialNo();
            PrivateKey privateKey = extConfig.getParsedMerchantPrivateKey();
            // 查询订单接口请求
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

            log.info("查询订单响应状态码: {}", response.getStatus());

            if (response.getStatus() == 200) {
                String body = response.getBody();
                JSONObject result = JSONUtil.parseObj(body);
                String tradeState = result.getStr("trade_state");
                String channelTradeNo = result.getStr("transaction_id");

                if ("SUCCESS".equals(tradeState)) {
                    // 支付成功
                    return PaymentResponseVO.builder()
                            .payNo(paymentChannelNo)
                            .mchId(mchId)
                            .channelTradeNo(channelTradeNo)
                            .payStatus(PayStatusEnum.PAY_SUCCESS.getValue())
                            .build();
                } else if ("NOTPAY".equals(tradeState)) {
                    // 未支付状态
                    return PaymentResponseVO.builder()
                            .payNo(paymentChannelNo)
                            .mchId(mchId)
                            .payStatus(PayStatusEnum.PAY_FAIL.getValue())
                            .build();
                } else {
                    // 其他状态视为支付失败
                    return PaymentResponseVO.builder()
                            .payNo(paymentChannelNo)
                            .mchId(mchId)
                            .payStatus(PayStatusEnum.PAY_FAIL.getValue())
                            .build();
                }
            } else {
                // 查询接口异常
                throw new BizException(PayResultCode.PAY_QUERY_STATUS_ERROR, String.valueOf(response.getStatus()));
            }
        } catch (Exception e) {
            log.error("查询微信小程序支付状态异常", e);
            throw new BizException(PayResultCode.PAY_QUERY_STATUS_ERROR);
        }
    }

    @Override
    public boolean closePayment(String paymentChannelNo) {
        logPayInfo("关闭微信小程序支付单", paymentChannelNo);

        try {
            if (currentConfig == null) {
                throw new BizException(PayResultCode.WX_CONFIG_NOT_FOUND);
            }

            WechatPayExtConfig extConfig = getExtConfig();
            String mchId = currentConfig.getMchId();
            String serialNo = extConfig.getMerchantSerialNo();
            PrivateKey privateKey = extConfig.getParsedMerchantPrivateKey();
            String domain = extConfig.getDomain() != null ? extConfig.getDomain() : WxDomainEnum.CHINA.toString();

            // 构建关闭订单请求参数
            JSONObject params = new JSONObject();
            params.set("mchid", mchId);

            // 关闭订单接口请求
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

            log.info("关闭订单响应状态码: {}", response.getStatus());

            // 关闭订单成功返回204状态码
            return response.getStatus() == 204;
        } catch (Exception e) {
            log.error("关闭微信小程序支付单异常", e);
            return false;
        }
    }

    @Override
    public boolean refund(PayRefundChannel refundRequest) {
        logPayInfo("申请微信小程序退款V3", refundRequest.getPaymentChannelNo());

        try {
            if (currentConfig == null) {
                throw new BizException(PayResultCode.WX_CONFIG_NOT_FOUND);
            }

            WechatPayExtConfig extConfig = getExtConfig();
            String mchId = currentConfig.getMchId();
            String serialNo = extConfig.getMerchantSerialNo();
            // 构建退款请求参数
            JSONObject params = new JSONObject();
            params.set("out_trade_no", refundRequest.getPaymentChannelNo());
            params.set("out_refund_no", refundRequest.getRefundChannelNo());

            // 退款金额信息（元转换为分）
            BigDecimal refundAmountInYuan = refundRequest.getAmount();
            int refundAmountInCents = refundAmountInYuan.multiply(BigDecimal.valueOf(100)).intValue();
            JSONObject amount = new JSONObject();
            amount.set("refund", refundAmountInCents);
            amount.set("total", refundAmountInCents);
            amount.set("currency", "CNY");
            params.set("amount", amount);

            // 退款原因
            // 退款原因在业务退款单中，这里暂时不设置

            // 退款通知地址
            if (StrUtil.isNotEmpty(currentConfig.getRefundNotifyUrl())) {
                params.set("notify_url", currentConfig.getRefundNotifyUrl());
            }

            // 退款接口请求
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

            log.info("申请退款响应状态码: {}", response.getStatus());

            if (response.getStatus() == 200) {
                String body = response.getBody();
                JSONObject result = JSONUtil.parseObj(body);
                log.info("申请退款结果: {}", result);
                String refundId = (String) result.get("refund_id");
                // 注意：策略方法不直接修改实体，退款ID由调用方处理
                refundRequest.setChannelTradeNo(refundId);

                String status = result.getStr("status");
                // 退款状态: SUCCESS(退款成功)、PROCESSING(退款处理中)、ABNORMAL(退款异常)
                return "SUCCESS".equals(status) || "PROCESSING".equals(status);
            } else {
                log.error("申请退款失败，状态码: {}, 响应: {}", response.getStatus(), response.getBody());
                return false;
            }
        } catch (Exception e) {
            log.error("申请微信小程序退款异常", e);
            return false;
        }
    }

    /**
     * 验证微信支付回调签名
     *
     * @param body        回调数据
     * @param timestamp   时间戳
     * @param nonce       随机字符串
     * @param signature   签名
     * @param certificate 平台证书
     * @return 验证结果
     */
    private boolean verifySignature(String body, String timestamp, String nonce, String signature, X509Certificate certificate) {
        try {
            if (StrUtil.isEmpty(body) || StrUtil.isEmpty(signature) || certificate == null) {
                return false;
            }

            // 构建验证字符串
            String message = timestamp + "\n" + nonce + "\n" + body + "\n";

            // 初始化签名对象
            java.security.Signature sign = java.security.Signature.getInstance("SHA256withRSA");
            sign.initVerify(certificate.getPublicKey());
            sign.update(message.getBytes(StandardCharsets.UTF_8));

            // 验证签名
            return sign.verify(java.util.Base64.getDecoder().decode(signature));
        } catch (Exception e) {
            log.error("验证签名异常", e);
            return false;
        }
    }

    @Override
    public RefundResultDTO refundCallback(HttpServletRequest request) {
        try {
            String body = getRequestBody(request);
            log.info("微信退款回调开始，请求数据：{}", body);

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

            log.error("处理微信退款回调失败，状态异常: refundStatus={}", refundStatus);
            return null;
        } catch (Exception e) {
            log.error("处理微信退款回调异常", e);
            return null;
        }
    }

    @Override
    public String getPayChannel() {
        return PayChannelEnum.WECHAT_JSAPI.getValue();
    }
}
