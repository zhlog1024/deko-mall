package com.deko.mall.pay.service.pay.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ijpay.core.kit.WxPayKit;
import com.deko.mall.common.rabbitmq.constant.MqBizConstants;
import com.deko.mall.common.rabbitmq.dto.MessageSendDTO;
import com.deko.mall.common.rabbitmq.service.MessageSendService;
import com.deko.mall.common.result.Result;
import com.deko.mall.common.result.ResultCode;
import com.deko.mall.common.utils.IdGenerator;
import com.deko.mall.common.security.util.SecurityUtils;
import com.deko.mall.common.web.exception.BizException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.deko.mall.pay.config.PayConfigManager;
import com.deko.mall.pay.api.dto.PaymentRequest;
import com.deko.mall.pay.api.enums.PayChannelEnum;
import com.deko.mall.pay.api.enums.PayStatusEnum;
import com.deko.mall.pay.api.enums.RefundStatusEnum;
import com.deko.mall.pay.api.dto.PaymentResultDTO;
import com.deko.mall.pay.api.dto.RefundRequestDTO;
import com.deko.mall.pay.api.dto.RefundResultDTO;
import com.deko.mall.pay.entity.PayConfig;
import com.deko.mall.pay.entity.PayPayment;
import com.deko.mall.pay.entity.PayPaymentChannel;
import com.deko.mall.pay.entity.PayRefund;
import com.deko.mall.pay.entity.PayRefundChannel;
import com.deko.mall.pay.mapper.PaymentMapper;
import com.deko.mall.pay.model.vo.CashierVO;
import com.deko.mall.pay.model.vo.PayChannelVO;
import com.deko.mall.pay.model.vo.PaymentResponseVO;
import com.deko.mall.pay.service.pay.*;
import com.deko.mall.pay.service.pay.strategy.PayStrategy;
import com.deko.mall.pay.service.pay.strategy.PayStrategyFactory;
import com.deko.mall.ums.api.MemberFeignClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import com.deko.mall.pay.config.PayResultCode;
import com.deko.mall.pay.model.dto.PayActionDTO;

/**
 * 支付服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PayServiceImpl implements PayService {

    private final PayStrategyFactory payStrategyFactory;
    private final IdGenerator idGenerator;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate redisTemplate;
    private final MemberFeignClient memberFeignClient;
    private final MessageSendService messageSendService;
    private final RefundService refundService;
    private final PaymentChannelService paymentChannelService;
    private final RefundChannelService refundChannelService;
    private final PaymentMapper paymentMapper;
    private final PayConfigManager payConfigManager;
    private final PayConfigService payConfigService;

    @Value("${pay.expire-minutes:28}")
    private Integer expireMinutes;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Override
    public CashierVO getCashierInfo(String paymentNo, String deviceType) {
        // 直接通过支付单号查询业务支付单
        PayPayment payment = paymentMapper.selectOne(new LambdaQueryWrapper<PayPayment>()
                .eq(PayPayment::getPaymentNo, paymentNo));
        if (payment == null) {
            throw new BizException(PayResultCode.PAY_ORDER_NOT_FOUND);
        }

        // 检查支付单状态
        if (payment.getStatus() == null) {
            throw new BizException(PayResultCode.PAY_ORDER_STATUS_ERROR, payment.getStatus());
        }
        if (payment.getStatus() != 0) {
            throw new BizException(PayResultCode.PAY_ORDER_STATUS_CANNOT_PAY, payment.getStatus());
        }

        CashierVO vo = new CashierVO();
        vo.setPaymentNo(paymentNo);
        vo.setPayAmount(payment.getAmount());
        vo.setOriginalAmount(payment.getOriginalPaymentAmount());
        vo.setPaymentDiscountAmount(payment.getPaymentDiscountAmount());

        // 从数据库 pay_config 查询启用 + 匹配当前端的渠道（deviceType 空=全端可用，不含 FULL_DEDUCTION）
        List<PayConfig> configs = payConfigService.lambdaQuery()
                .eq(PayConfig::getEnabled, true)
                .ne(PayConfig::getPayChannel, PayChannelEnum.FULL_DEDUCTION.getValue())
                .and(w -> w.isNull(PayConfig::getDeviceType).or().like(PayConfig::getDeviceType, deviceType))
                .list();

        if (configs.isEmpty()) {
            log.warn("支付单 {} 没有可用的支付渠道，设备类型：{}", paymentNo, deviceType);
            throw new BizException(PayResultCode.PAY_NO_AVAILABLE_METHOD);
        }

        // 按用户白名单过滤渠道
        Long memberId = SecurityUtils.getMemberId();
        configs = configs.stream()
                .filter(config -> {
                    String allowed = config.getAllowedUserIds();
                    if (allowed == null || allowed.isBlank()) {
                        return true; // 未设置白名单，所有用户可见
                    }
                    if (memberId == null) {
                        return false; // 设置了白名单但用户未登录
                    }
                    boolean matched = Arrays.stream(allowed.split(","))
                            .map(String::trim)
                            .anyMatch(id -> id.equals(String.valueOf(memberId)));
                    if (!matched) {
                        log.debug("用户 {} 不在渠道 {} 的白名单中，已过滤", memberId, config.getPayChannel());
                    }
                    return matched;
                })
                .collect(Collectors.toList());

        if (configs.isEmpty()) {
            log.warn("支付单 {} 所有渠道均被白名单过滤，memberId={}", paymentNo, memberId);
            throw new BizException(PayResultCode.PAY_NO_AVAILABLE_METHOD);
        }

        List<PayChannelVO> channels = new ArrayList<>();
        for (PayConfig config : configs) {
            PayChannelVO p = new PayChannelVO();
            p.setChannelCode(config.getPayChannel());
            p.setChannelName(config.getCashierName() != null ? config.getCashierName() : config.getPayChannel());
            p.setChannelIcon(config.getChannelIcon());
            p.setDeviceType(deviceType);
            p.setEnabled(true);
            // 线下支付：extConfig（银行信息）通过 meta 传给前端
            if (PayChannelEnum.OFFLINE_PAY.getValue().equals(config.getPayChannel())
                    && config.getExtConfig() != null) {
                try {
                    p.setMeta(JSONUtil.toBean(config.getExtConfig(), Map.class));
                } catch (Exception e) {
                    log.warn("线下支付 extConfig 解析失败: {}", e.getMessage());
                }
            }
            channels.add(p);
        }

        vo.setChannels(channels);

        // 如果已有渠道请求则把其 payNo/payParam/mchId 暴露到 CashierVO 的扩展字段（若有）
        // if request exists, front-end can fetch request params via separate API if needed
        return vo;
    }

    @Override
    public PaymentResponseVO getPaymentStatus(String paymentNo) {
        PayPayment payment = paymentMapper.selectOne(new LambdaQueryWrapper<PayPayment>()
                .eq(PayPayment::getPaymentNo, paymentNo));
        if (payment == null) {
            throw new BizException(PayResultCode.PAY_ORDER_NOT_FOUND);
        }
        return PaymentResponseVO.builder()
                .payNo(paymentNo)
                .payStatus(payment.getStatus())
                .build();
    }

    @Override
    public PaymentResponseVO paymentRequest(PayActionDTO dto) {
        String channel = dto.getChannel();
        String paymentNo = dto.getPaymentNo();
        String code = dto.getCode();
        String deviceType = dto.getDeviceType();
        long startTime = System.currentTimeMillis();
        log.info("开始处理支付请求, channel={}, paymentNo={}, hasCode={}", channel, paymentNo, StrUtil.isNotBlank(code));

        // 检查支付单是否已有未完成的支付
        String lockKey = "pay:payment:" + paymentNo;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (!lock.tryLock(10, TimeUnit.SECONDS)) {
                log.warn("获取支付锁失败，支付单正在处理中, paymentNo={}, channel={}", paymentNo, channel);
                throw new BizException(PayResultCode.PAY_ORDER_IN_PROGRESS);
            }
            log.debug("成功获取支付锁, paymentNo={}, channel={}", paymentNo, channel);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取支付锁时被中断, paymentNo={}, channel={}", paymentNo, channel);
            throw new BizException(PayResultCode.PAY_LOCK_FAILED);
        }

        try {
            // 获取业务支付单
            PayPayment businessPayment = paymentMapper.selectOne(new LambdaQueryWrapper<PayPayment>()
                    .eq(PayPayment::getPaymentNo, paymentNo));
            if (businessPayment == null) {
                throw new BizException(PayResultCode.PAY_ORDER_NOT_FOUND);
            }

            // 优先检查是否已有未完成的渠道请求（基于业务支付单号和渠道）
            PayPaymentChannel unPaidRequest = paymentChannelService.lambdaQuery()
                    .eq(PayPaymentChannel::getPaymentNo, businessPayment.getPaymentNo())
                    .eq(PayPaymentChannel::getPaymentChannel, channel)
                    .eq(PayPaymentChannel::getStatus, PayStatusEnum.WAIT_PAY.getValue())
                    .orderByDesc(PayPaymentChannel::getCreateTime)
                    .one();
            if (unPaidRequest != null && StrUtil.isNotBlank(unPaidRequest.getPayParam())) {
                log.info("复用已存在的支付请求, paymentNo={}, channel={}, requestNo={}",
                        paymentNo, channel, unPaidRequest.getPaymentChannelNo());
                return JSONUtil.toBean(unPaidRequest.getPayParam(), PaymentResponseVO.class);
            }

            // 从业务支付单获取支付描述（避免直接依赖订单服务）
            String payContent = StrUtil.isNotBlank(businessPayment.getPaymentContent()) ? businessPayment.getPaymentContent() : "商品";

            // 获取用户渠道信息（如微信openid）
            String channelUserId = getChannelUserId(channel, code);

            // 获取客户端IP地址
            String clientIp = getClientIp();

            // 创建并保存渠道支付请求（在事务中执行）
            PayPaymentChannel paymentRequest = createPaymentChannelRequest(businessPayment, channel, payContent, channelUserId, clientIp, deviceType);

            // 线下支付：将凭证URL存入 payParam，更新状态为 PAYING，不设 expireTime
            if (PayChannelEnum.OFFLINE_PAY.getValue().equals(channel)) {
                if (StrUtil.isNotBlank(code)) {
                    paymentRequest.setPayParam(code);
                }
                paymentRequest.setStatus(PayStatusEnum.PAYING.getValue());
                paymentRequest.setExpireTime(null);  // 不自动过期，等人工审核
                paymentChannelService.updateById(paymentRequest);
                businessPayment.setStatus(PayStatusEnum.PAYING.getValue());
                paymentMapper.updateById(businessPayment);
            }

            // 调用支付渠道（事务外执行，避免外部调用影响事务）
            PayStrategy strategy = getStrategy(channel);
            PaymentResponseVO payVO = strategy.paymentRequest(paymentRequest);

            // 更新渠道请求的支付参数（在事务中执行）
            updatePaymentChannelRequest(paymentRequest, payVO);

            // 全额支付特殊处理：如果是全抵扣且支付成功，延迟执行后续处理
            if (PayChannelEnum.FULL_DEDUCTION.getValue().equals(channel) &&
                    payVO.getPayStatus() != null &&
                    PayStatusEnum.PAY_SUCCESS.getValue().equals(payVO.getPayStatus())) {
                log.info("全额抵扣成功，延迟处理后续逻辑，请求号：{}", paymentRequest.getPaymentChannelNo());

                // 生成渠道交易号(全额抵扣使用请求号作为渠道交易号)
                String channelTradeNo = paymentRequest.getPaymentChannelNo();
                String notifyData = "{\"requestNo\":\"" + paymentRequest.getPaymentChannelNo() + "\",\"paymentNo\":\"" + paymentRequest.getPaymentNo() + "\",\"status\":\"success\",\"channelTradeNo\":\"FULL_DEDUCTION_" + paymentRequest.getPaymentChannelNo() + "\"}";
                handlePaySuccess(paymentRequest, channelTradeNo, notifyData);
                log.info("全额抵扣成功逻辑处理完成，请求号: {}", paymentRequest.getPaymentChannelNo());
            }

            long endTime = System.currentTimeMillis();
            log.info("支付请求处理完成, channel={}, paymentNo={}, 耗时={}ms", channel, paymentNo, endTime - startTime);
            return payVO;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            log.error("支付请求处理异常, channel={}, paymentNo={}, 耗时={}ms, error={}",
                    channel, paymentNo, endTime - startTime, e.getMessage(), e);
            throw e;
        } finally {
            try {
                lock.unlock();
                log.debug("释放支付锁成功, paymentNo={}, channel={}", paymentNo, channel);
            } catch (Exception e) {
                log.warn("释放支付锁失败, paymentNo={}, channel={}, error={}", paymentNo, channel, e.getMessage());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean paymentResult(String payChannel, HttpServletRequest request) {
        try {

            PaymentResultDTO payCallback;

            //真实上线后 恢复环境限制
            if ("mock".equals(payChannel)) {
                String xmlData = request.getReader().lines().collect(Collectors.joining());
                log.info("支付回调 - 支付渠道：{}，回调数据：{}", payChannel, xmlData);
                // 1. 检查是否是模拟回调
                Map<String, String> params = WxPayKit.xmlToMap(xmlData);

                log.info("支付回调 - 支付渠道：{}，回调数据：{}", payChannel, params);


                // 模拟回调数据
                String payNo = params.get("payNo");
                if (StrUtil.isBlank(payNo)) {
                    log.warn("模拟回调参数错误，支付单号为空");
                    return false;
                }

                payCallback = new PaymentResultDTO();
                payCallback.setPaymentChannelNo(payNo);
                payCallback.setStatus(PayStatusEnum.PAY_SUCCESS.getValue());
                payCallback.setChannelTradeNo(idGenerator.generatePayNo());
                payCallback.setPayTime(LocalDateTime.now());
                payCallback.setNotifyData("mock payment callback");

                log.info("模拟支付回调 - 支付单号：{}", payNo);

            } else {
                // 2. 真实支付回调
                PayStrategy strategy = getStrategy(payChannel);
                log.info("支付回调分发 渠道={} strategy={}", payChannel, strategy.getClass().getSimpleName());
                payCallback = strategy.paymentResult(request);
            }

            // 3. 检查回调结果
            if (payCallback == null) {
                log.warn("支付回调处理失败，回调数据为空");
                return false;
            }

            if (PayStatusEnum.PAY_FAIL.getValue().equals(payCallback.getStatus())) {
                log.warn("支付失败: {}", payCallback.getPaymentChannelNo());
                return false;
            }

            // 4. 获取渠道请求记录（payNo 现在是 requestNo）
            String requestNo = payCallback.getPaymentChannelNo();
            PayPaymentChannel paymentRequest = paymentChannelService.lambdaQuery()
                    .eq(PayPaymentChannel::getPaymentChannelNo, requestNo)
                    .one();
            if (paymentRequest == null) {
                log.error("渠道请求记录不存在: {}", requestNo);
                throw new BizException(PayResultCode.CHANNEL_REQUEST_NOT_FOUND);
            }

            // 5. 检查渠道请求状态
            if (!PayStatusEnum.WAIT_PAY.getValue().equals(paymentRequest.getStatus())) {
                log.warn("渠道请求状态不正确: {}, 当前状态: {}", requestNo, paymentRequest.getStatus());
                throw new BizException(PayResultCode.CHANNEL_REQUEST_STATUS_INVALID);
            }

            // 6. 幂等性检查
            String idempotentKey = "pay:callback:" + requestNo;
            Boolean setResult = redisTemplate.opsForValue().setIfAbsent(idempotentKey, "1", 24, TimeUnit.HOURS);
            if (Boolean.FALSE.equals(setResult)) {
                log.warn("重复的支付回调请求: {}", requestNo);
                return false;
            }

            // 如果Redis操作失败（返回null），仍然继续处理
            // 这可能导致重复处理，但是比直接失败好

            // 7. 如果支付成功，处理成功逻辑
            if (PayStatusEnum.PAY_SUCCESS.getValue().equals(payCallback.getStatus())) {
                // 调用支付成功处理逻辑，包含支付记录更新、订单状态更新和发送MQ消息
                handlePaySuccess(paymentRequest, payCallback.getChannelTradeNo(), payCallback.getNotifyData());
            } else {
                // 其他支付状态，只更新渠道请求记录
                log.info("支付状态更新为非成功状态: requestNo={}, status={}, paymentNo={}",
                        paymentRequest.getPaymentChannelNo(), payCallback.getStatus(), paymentRequest.getPaymentNo());

                paymentRequest.setStatus(payCallback.getStatus());
                paymentRequest.setNotifyParam(payCallback.getNotifyData());
                paymentRequest.setChannelTradeNo(payCallback.getChannelTradeNo());
                paymentRequest.setUpdateTime(LocalDateTime.now());
                paymentChannelService.updateById(paymentRequest);

                log.info("渠道支付请求状态已更新: requestNo={}, status={}",
                        paymentRequest.getPaymentChannelNo(), payCallback.getStatus());
            }
            return true;
        } catch (Exception e) {
            log.error("处理支付回调异常", e);
            throw new BizException(PayResultCode.PAY_CALLBACK_EXCEPTION, e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean refundResult(RefundResultDTO refundResultDTO) {
        // 分布式锁防止重复处理
        String lockKey = "refund_callback:" + refundResultDTO.getApplyNo();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试获取锁，阻止重复处理
            boolean locked = lock.tryLock(5, 30, TimeUnit.SECONDS);
            if (!locked) {
                log.warn("退款回调正在被处理，跳过重复请求，退款单号: {}", refundResultDTO.getRefundNo());
                return true; // 返回true表示处理成功，避免支付平台重试
            }

            log.info("开始处理退款回调，退款单号: {}, 支付单号: {}",
                    refundResultDTO.getRefundNo(), refundResultDTO.getPayNo());

            // 1. 校验退款记录存在性
            PayRefund refund = refundService.getByRefundNo(refundResultDTO.getRefundNo());
            if (refund == null) {
                log.error("退款记录不存在: {}", refundResultDTO.getRefundNo());
                return false;
            }

            // 2. 检查并发状态，如果已处理则跳过
            if (RefundStatusEnum.REFUND_SUCCESS.getValue().equals(refund.getStatus())) {
                log.info("退款已处理成功，跳过重复处理，退款单号: {}", refundResultDTO.getRefundNo());
                return true;
            }

            // 3. 更新退款记录状态 - 只处理支付相关的状态更新
            refund.setStatus(RefundStatusEnum.REFUND_SUCCESS.getValue());
            refund.setUpdateTime(LocalDateTime.now());
            // 更新申请单号用于追踪
            refund.setReason(refund.getReason() + " [applyNo:" + refundResultDTO.getApplyNo() + "]");

            boolean updated = refundService.updateById(refund);
            if (!updated) {
                log.error("更新退款记录失败: {}", refundResultDTO.getRefundNo());
                return false;
            }

            // 4. 发送退款成功消息，订单服务通过监听消息处理订单和售后状态更新
            try {
                // 发送退款成功消息到消息队列
                messageSendService.sendMessage(MessageSendDTO.builder()
                .exchange(MqBizConstants.RefundResult.EXCHANGE)
                .routingKey(MqBizConstants.RefundResult.ROUTING_KEY)
                .payload(refundResultDTO)
                .businessId(refundResultDTO.getApplyNo())
                .messageType(MqBizConstants.MessageType.REFUND_RESULT)
                .source(MqBizConstants.MessageSource.OMS_SERVICE)
                .build());
                log.info("退款成功消息已发送: refundNo={}", refundResultDTO.getRefundNo());
            } catch (Exception e) {
                log.error("退款成功消息发送失败: refundNo={}, error={}",
                        refundResultDTO.getRefundNo(), e.getMessage(), e);
                // 消息发送失败不影响主流程
            }

            log.info("退款回调处理成功，退款单号: {}", refundResultDTO.getRefundNo());
            return true;
        } catch (Exception e) {
            log.error("处理退款回调异常", e);
            throw new BizException(PayResultCode.PAY_REFUND_CALLBACK_EXCEPTION, e.getMessage());
        } finally {
            // 释放锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("释放退款回调锁，退款单号: {}", refundResultDTO.getRefundNo());
            }
        }
    }

    @Override
    public RefundResultDTO refundCallback(String channel, HttpServletRequest request) {
        log.info("处理{}渠道退款回调", channel);

        PayStrategy strategy = getStrategy(channel);

        // 策略类处理退款回调
        RefundResultDTO resultDTO = strategy.refundCallback(request);

        if (resultDTO != null && resultDTO.getRefundNo() != null) {
            // 回调返回的 refundNo 实际是渠道退款请求号(refundChannelNo)，
            // 需要通过它找到业务退款记录，补全 applyNo/orderNo/refundNo 等字段
            String channelRefundNo = resultDTO.getRefundNo();
            PayRefundChannel channelRefund = refundChannelService.getOne(
                    new LambdaQueryWrapper<PayRefundChannel>()
                            .eq(PayRefundChannel::getRefundChannelNo, channelRefundNo)
            );
            if (channelRefund != null) {
                PayRefund businessRefund = refundService.getOne(
                        new LambdaQueryWrapper<PayRefund>()
                                .eq(PayRefund::getRefundNo, channelRefund.getRefundNo())
                );
                if (businessRefund != null) {
                    resultDTO.setRefundNo(businessRefund.getRefundNo());
                    resultDTO.setApplyNo(businessRefund.getApplyNo());
                    resultDTO.setOrderNo(businessRefund.getOrderNo());
                }
                refundResult(resultDTO);
            } else {
                log.error("渠道退款记录不存在: channelRefundNo={}", channelRefundNo);
            }
        }

        return resultDTO;
    }

    @Override
    public PaymentResponseVO queryStatus(String requestNo) {
        PayPaymentChannel record = paymentChannelService.lambdaQuery()
                .eq(PayPaymentChannel::getPaymentChannelNo, requestNo)
                .one();
        if (record == null) {
            throw new BizException(PayResultCode.CHANNEL_REQUEST_NOT_FOUND);
        }

        PayStrategy strategy = getStrategy(record.getPaymentChannel());
        return strategy.queryStatus(requestNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean closePayment(String requestNo) {
        PayPaymentChannel record = paymentChannelService.lambdaQuery()
                .eq(PayPaymentChannel::getPaymentChannelNo, requestNo)
                .one();
        if (record == null) {
            return true; // 无需关闭
        }

        // 已关闭则跳过
        if (PayStatusEnum.CLOSED.getValue().equals(record.getStatus())) {
            log.info("支付渠道请求已关闭，跳过: {}", requestNo);
            return true;
        }

        // 调用对应渠道的关闭接口
        try {
            PayStrategy strategy = getStrategy(record.getPaymentChannel());
            log.info("调用支付渠道{}关闭接口，请求号: {}", record.getPaymentChannel(), requestNo);
            strategy.closePayment(requestNo);
        } catch (Exception e) {
            log.warn("调用支付渠道关闭接口失败，直接标记为已关闭: requestNo={}, channel={}, error={}",
                    requestNo, record.getPaymentChannel(), e.getMessage());
        }

        record.setStatus(PayStatusEnum.CLOSED.getValue());
        record.setUpdateTime(LocalDateTime.now());
        paymentChannelService.updateById(record);
        return true;
    }


    private PayStrategy getStrategy(String channel) {
        log.info("Getting strategy for channel: {}", channel);
        return payStrategyFactory.getStrategy(channel);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean refundRequest(RefundRequestDTO refundRequestDTO) {
        log.info("通过DTO执行退款操作，订单号: {}, 退款单号: {}, 退款金额: {}",
                refundRequestDTO.getOrderNo(), refundRequestDTO.getRefundNo(), refundRequestDTO.getRefundAmount());

        // 直接通过paymentNo查询业务支付单
        PayPayment businessPayment = paymentMapper.selectOne(new LambdaQueryWrapper<PayPayment>()
                .eq(PayPayment::getPaymentNo, refundRequestDTO.getPaymentNo())
                .eq(PayPayment::getStatus, PayStatusEnum.PAY_SUCCESS.getValue())
                .orderByDesc(PayPayment::getCreateTime));


        if (businessPayment == null) {
            log.error("未找到有效的业务支付单，无法退款，订单号: {}", refundRequestDTO.getOrderNo());
            throw new BizException(PayResultCode.REFUND_NOT_FOUND);
        }

        // 通过业务支付单找到成功的渠道请求
        PayPaymentChannel paymentRequest = paymentChannelService.lambdaQuery()
                .eq(PayPaymentChannel::getPaymentNo, businessPayment.getPaymentNo())
                .eq(PayPaymentChannel::getStatus, PayStatusEnum.PAY_SUCCESS.getValue())
                .orderByDesc(PayPaymentChannel::getCreateTime)
                
                .one();

        if (paymentRequest == null) {
            log.error("未找到有效的渠道支付请求，无法退款，业务支付单号: {}", businessPayment.getPaymentNo());
            throw new BizException(PayResultCode.REFUND_CHANNEL_NOT_FOUND);
        }

        // 计算退款金额：如果未指定，则使用支付金额（全额退款）
        BigDecimal refundAmount = refundRequestDTO.getRefundAmount();
        if (refundAmount == null) {
            refundAmount = paymentRequest.getAmount();
            log.info("未指定退款金额，使用支付金额作为退款金额: {}", refundAmount);
        }

        // 创建业务退款记录（oms_refund）
        PayRefund businessRefund = new PayRefund();
        businessRefund.setRefundNo(idGenerator.generateRefundNo());
        businessRefund.setPaymentNo(businessPayment.getPaymentNo());
        businessRefund.setOrderNo(refundRequestDTO.getOrderNo()); // 从DTO获取
        businessRefund.setApplyNo(refundRequestDTO.getApplyNo()); // 申请单号（售后单号）
        businessRefund.setMemberId(businessPayment.getMemberId());
        businessRefund.setRefundAmount(refundAmount);
        businessRefund.setStatus(RefundStatusEnum.REFUNDING.getValue()); // 退款中
        businessRefund.setReason(refundRequestDTO.getRefundReason());
        businessRefund.setCreateTime(LocalDateTime.now());
        businessRefund.setUpdateTime(LocalDateTime.now());
        refundService.save(businessRefund);

        // 创建渠道退款请求记录（oms_refund_request）
        PayRefundChannel refundRequest = new PayRefundChannel();
        refundRequest.setRefundChannelNo(idGenerator.generateRefundTransactionNo()); // 生成渠道退款请求号
        refundRequest.setRefundNo(businessRefund.getRefundNo());
        refundRequest.setPaymentNo(businessPayment.getPaymentNo());
        refundRequest.setPaymentChannelNo(paymentRequest.getPaymentChannelNo());
        refundRequest.setPaymentChannel(paymentRequest.getPaymentChannel());
        // 使用已计算的退款金额（如果 DTO 中未指定，则已回退为支付金额）
        refundRequest.setAmount(refundAmount);
        refundRequest.setStatus(RefundStatusEnum.REFUNDING.getValue());
        refundRequest.setCreateTime(LocalDateTime.now());
        refundRequest.setUpdateTime(LocalDateTime.now());
        // 先保存渠道退款请求记录，后续会根据结果更新状态
        refundChannelService.save(refundRequest);

        try {
            // 调用退款接口
            PayStrategy strategy = getStrategy(paymentRequest.getPaymentChannel());
            boolean success = strategy.refund(refundRequest);

            if (!success) {
                log.error("退款申请处理失败，退款单号: {}", businessRefund.getRefundNo());
                // 更新渠道退款请求状态为失败
                refundRequest.setStatus(RefundStatusEnum.REFUND_FAIL.getValue());
                refundRequest.setUpdateTime(LocalDateTime.now());
                refundChannelService.updateById(refundRequest);
                return false;
            }

            // 全抵扣无第三方，直接完成
            if (PayChannelEnum.FULL_DEDUCTION.getValue().equals(paymentRequest.getPaymentChannel())) {
                RefundResultDTO refundResultDTO = new RefundResultDTO()
                        .setRefundNo(businessRefund.getRefundNo())
                        .setPayNo(businessPayment.getPaymentNo())
                        .setApplyNo(refundRequestDTO.getApplyNo())
                        .setOrderNo(refundRequestDTO.getOrderNo())
                        .setRefundAmount(businessRefund.getRefundAmount())
                        .setStatus(RefundStatusEnum.REFUND_SUCCESS.getValue())
                        .setChannelTradeNo(refundRequest.getRefundChannelNo())
                        .setRefundType(refundRequestDTO.getRefundType());
                this.refundResult(refundResultDTO);
                return true;
            }

            // 其他渠道：终态由回调通知驱动
            log.info("退款已受理，等待渠道回调: refundNo={}", businessRefund.getRefundNo());
            return true;
        } catch (Exception e) {
            log.error("退款申请处理异常: {}", e.getMessage(), e);
            // 更新渠道退款请求状态为失败
            refundRequest.setStatus(RefundStatusEnum.REFUND_FAIL.getValue());
            refundRequest.setUpdateTime(LocalDateTime.now());
            refundChannelService.updateById(refundRequest);
            throw new BizException(PayResultCode.REFUND_APPLY_FAILED, e.getMessage());
        }
    }

    /**
     * 处理支付成功后的业务逻辑，包括支付记录状态更新、订单状态更新和通知发送
     *
     * @param paymentRequest 支付记录
     * @param channelTradeNo 支付渠道交易号
     * @param notifyData     回调通知数据
     * @return 处理结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean handlePaySuccess(PayPaymentChannel paymentRequest, String channelTradeNo, String notifyData) {
        long startTime = System.currentTimeMillis();
        try {
            log.info("开始处理支付成功逻辑，请求号: {}, 支付单号: {}, 渠道交易号: {}, 渠道: {}",
                    paymentRequest.getPaymentChannelNo(), paymentRequest.getPaymentNo(),
                    channelTradeNo, paymentRequest.getPaymentChannel());

            // 1. 更新渠道请求状态
            paymentRequest.setStatus(PayStatusEnum.PAY_SUCCESS.getValue());
            paymentRequest.setChannelTradeNo(channelTradeNo);
            if (notifyData != null) {
                paymentRequest.setNotifyParam(notifyData);
            }
            paymentChannelService.updateById(paymentRequest);

            // 2. 更新业务支付单状态
            PayPayment businessPayment = paymentMapper.selectOne(new LambdaQueryWrapper<PayPayment>()
                    .eq(PayPayment::getPaymentNo, paymentRequest.getPaymentNo()));
            if (businessPayment != null) {
                businessPayment.setStatus(PayStatusEnum.PAY_SUCCESS.getValue());
                businessPayment.setPaymentChannel(paymentRequest.getPaymentChannel());
                businessPayment.setPaidTime(LocalDateTime.now());
                paymentMapper.updateById(businessPayment);
            }

            // 3. 删除支付过期key
            String expireKey = "pay:expire:" + paymentRequest.getPaymentChannelNo();
            redisTemplate.delete(expireKey);

            // 4. 发送支付成功消息（直接发送paymentNo，让订单模块处理）
            try {
                // 创建支付结果DTO
                PaymentResultDTO paymentResult = new PaymentResultDTO();
                paymentResult.setPaymentChannelNo(paymentRequest.getPaymentChannelNo());
                paymentResult.setPaymentNo(paymentRequest.getPaymentNo());
                paymentResult.setChannelTradeNo(channelTradeNo);
                paymentResult.setStatus(PayStatusEnum.PAY_SUCCESS.getValue());
                paymentResult.setPayTime(LocalDateTime.now());
                paymentResult.setNotifyData(notifyData);
                paymentResult.setPayChannel(paymentRequest.getPaymentChannel());
                paymentResult.setMchId(paymentRequest.getMchId());
                paymentResult.setChannelUserId(paymentRequest.getChannelUserId());

                messageSendService.sendMessage(MessageSendDTO.builder()
                .exchange(MqBizConstants.PaymentResult.EXCHANGE) .routingKey(MqBizConstants.PaymentResult.ROUTING_KEY) .payload(paymentResult) .businessId(paymentRequest.getPaymentNo()) .messageType(MqBizConstants.MessageType.PAYMENT_RESULT) .source(MqBizConstants.MessageSource.OMS_SERVICE)
                .build());
                log.info("支付成功消息已发送: paymentNo={}, type={}",
                        paymentRequest.getPaymentNo(), MqBizConstants.MessageType.PAYMENT_RESULT);
            } catch (Exception e) {
                log.error("支付成功消息发送失败: paymentNo={}, type={}, error={}",
                        paymentRequest.getPaymentNo(), MqBizConstants.MessageType.PAYMENT_RESULT, e.getMessage(), e);
                // 消息发送失败不影响主流程
            }
            long endTime = System.currentTimeMillis();
            log.info("支付成功逻辑处理完成，请求号: {}, 支付单号: {}, 耗时={}ms",
                    paymentRequest.getPaymentChannelNo(), paymentRequest.getPaymentNo(), endTime - startTime);
            return true;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            log.error("处理支付成功后续逻辑异常，请求号: {}, 支付单号: {}, 耗时={}ms, error={}",
                    paymentRequest.getPaymentChannelNo(), paymentRequest.getPaymentNo(),
                    endTime - startTime, e.getMessage(), e);
            throw new BizException(PayResultCode.PAY_POST_PROCESS_ERROR, e.getMessage());
        }
    }


    /**
     * 查找业务支付单（极简版）
     * 直接通过订单号或父订单号查找支付单
     *
     * @param orderNo 订单号
     * @return 业务支付单
     */

    /**
     * 获取渠道用户信息（如微信openid）
     */
    private String getChannelUserId(String channel, String code) {
        if (channel.equals(PayChannelEnum.WECHAT_JSAPI.getValue()) && StrUtil.isNotBlank(code)) {
            // 获取用户微信openid
            Result<String> result = memberFeignClient.getOpenidByCode(code);
            if (ResultCode.SUCCESS.getCode().equals(result.getCode())) {
                return result.getData();
            }
        }
        return "";
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                // 获取真实IP（考虑代理场景）
                String ip = request.getHeader("X-Forwarded-For");
                if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("Proxy-Client-IP");
                }
                if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("WL-Proxy-Client-IP");
                }
                if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("HTTP_CLIENT_IP");
                }
                if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("HTTP_X_FORWARDED_FOR");
                }
                if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                // 多个代理时，第一个IP为真实IP
                if (StrUtil.isNotBlank(ip) && ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        } catch (Exception e) {
            log.warn("获取客户端IP失败", e);
        }
        return "127.0.0.1";
    }

    /**
     * 创建并保存渠道支付请求
     */
    @Transactional(rollbackFor = Exception.class)
    private PayPaymentChannel createPaymentChannelRequest(PayPayment businessPayment, String channel, String payContent, String channelUserId, String clientIp, String deviceType) {
        // 幂等检查：再次确认没有其他并发请求创建了相同的渠道请求
        PayPaymentChannel existingRequest = paymentChannelService.lambdaQuery()
                .eq(PayPaymentChannel::getPaymentNo, businessPayment.getPaymentNo())
                .eq(PayPaymentChannel::getPaymentChannel, channel)
                .eq(PayPaymentChannel::getStatus, PayStatusEnum.WAIT_PAY.getValue())
                .orderByDesc(PayPaymentChannel::getCreateTime)
                
                .one();

        if (existingRequest != null) {
            log.info("检测到并发创建的支付请求，直接复用, paymentNo={}, channel={}, requestNo={}",
                    businessPayment.getPaymentNo(), channel, existingRequest.getPaymentChannelNo());
            return existingRequest;
        }

        // 创建渠道支付请求
        PayPaymentChannel paymentRequest = new PayPaymentChannel();
        paymentRequest.setPaymentChannelNo(idGenerator.generatePayTransactionNo());
        paymentRequest.setPaymentNo(businessPayment.getPaymentNo());
        paymentRequest.setMemberId(businessPayment.getMemberId());
        paymentRequest.setAmount(businessPayment.getAmount());
        paymentRequest.setPaymentChannel(channel);
        paymentRequest.setPaymentContent(payContent);
        paymentRequest.setChannelUserId(channelUserId);
        paymentRequest.setClientIp(clientIp);
        paymentRequest.setDeviceType(deviceType);        paymentRequest.setStatus(PayStatusEnum.WAIT_PAY.getValue());
        paymentRequest.setCreateTime(LocalDateTime.now());
        paymentRequest.setUpdateTime(LocalDateTime.now());
        paymentRequest.setExpireTime(LocalDateTime.now().plusMinutes(expireMinutes));

        // 设置支付超时时间
        String expireKey = "pay:expire:" + paymentRequest.getPaymentChannelNo();
        redisTemplate.opsForValue().set(expireKey, "1", expireMinutes, TimeUnit.MINUTES);

        // 保存渠道请求（使用saveOrUpdate以防并发）
        boolean saved = paymentChannelService.save(paymentRequest);
        if (!saved) {
            log.error("保存支付渠道请求失败, paymentNo={}, channel={}", businessPayment.getPaymentNo(), channel);
            throw new BizException(PayResultCode.CHANNEL_REQUEST_CREATE_FAILED);
        }

        log.info("创建支付渠道请求成功, paymentNo={}, channel={}, requestNo={}",
                businessPayment.getPaymentNo(), channel, paymentRequest.getPaymentChannelNo());
        return paymentRequest;
    }

    /**
     * 更新渠道支付请求的支付参数
     */
    @Transactional(rollbackFor = Exception.class)
    private void updatePaymentChannelRequest(PayPaymentChannel paymentRequest, PaymentResponseVO payVO) {
        // 存储完整响应 JSON，复用未完成支付请求时直接反序列化
        paymentRequest.setPayParam(JSONUtil.toJsonStr(payVO));
        paymentRequest.setMchId(payVO.getMchId());
        paymentRequest.setUpdateTime(LocalDateTime.now());

        boolean update = paymentChannelService.updateById(paymentRequest);
        if (!update) {
            throw new BizException(PayResultCode.CHANNEL_REQUEST_UPDATE_FAILED);
        }

        log.info("更新支付渠道请求成功, requestNo={}",
                paymentRequest.getPaymentChannelNo());
    }

    /**
     * 检查支付渠道是否有启用的配置
     *
     * @param channelCode 渠道代码
     * @return true 表示有启用配置，false 表示没有启用配置
     */
    private boolean hasEnabledConfig(String channelCode) {
        try {
            // PayConfigManager 只缓存启用的配置，如果 getConfig 不抛异常说明有启用配置
            payConfigManager.getConfig(channelCode);
            return true;
        } catch (BizException e) {
            log.debug("支付渠道[{}]没有启用的配置", channelCode);
            return false;
        }
    }



    @Override
    public boolean updatePaymentAmount(PaymentRequest request) {
        var payment = paymentMapper.selectOne(
                new LambdaQueryWrapper<PayPayment>().eq(PayPayment::getPaymentNo, request.getPaymentNo()));
        if (payment == null) return false;
        payment.setAmount(request.getPayAmount());
        payment.setUpdateTime(LocalDateTime.now());
        paymentMapper.updateById(payment);
        return true;
    }

    /**
     * 关闭支付（按业务支付单号，关闭所有未完成的渠道请求和支付单）
     */
    @Override
    public void closePaymentByPaymentNo(String paymentNo) {
        // 关闭所有未完成的渠道请求
        List<PayPaymentChannel> channels = paymentChannelService.lambdaQuery()
                .eq(PayPaymentChannel::getPaymentNo, paymentNo)
                .in(PayPaymentChannel::getStatus, PayStatusEnum.WAIT_PAY.getValue(), PayStatusEnum.PAYING.getValue())
                .list();
        for (PayPaymentChannel channel : channels) {
            channel.setStatus(PayStatusEnum.CLOSED.getValue());
            paymentChannelService.updateById(channel);
            log.info("渠道支付请求已关闭, paymentChannelNo={}, paymentNo={}", channel.getPaymentChannelNo(), paymentNo);
        }

        // 关闭业务支付单
        PayPayment payment = paymentMapper.selectOne(
                new LambdaQueryWrapper<PayPayment>().eq(PayPayment::getPaymentNo, paymentNo));
        if (payment != null && !PayStatusEnum.PAY_SUCCESS.getValue().equals(payment.getStatus())) {
            payment.setStatus(PayStatusEnum.CLOSED.getValue());
            paymentMapper.updateById(payment);
            log.info("业务支付单已关闭, paymentNo={}", paymentNo);
        }
    }

    /**
     * 回退支付单状态到待支付（线下支付审核驳回时使用）
     */
    @Override
    public void resetPaymentToWaitPay(String paymentNo) {
        PayPayment payment = paymentMapper.selectOne(
                new LambdaQueryWrapper<PayPayment>().eq(PayPayment::getPaymentNo, paymentNo));
        if (payment != null && PayStatusEnum.PAYING.getValue().equals(payment.getStatus())) {
            payment.setStatus(PayStatusEnum.WAIT_PAY.getValue());
            paymentMapper.updateById(payment);
            log.info("线下支付驳回，回退支付单状态, paymentNo={}", paymentNo);
        }
    }
}
