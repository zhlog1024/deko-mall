package com.deko.mall.pay.controller.feign;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.deko.mall.common.result.Result;
import com.deko.mall.common.utils.IdGenerator;
import com.deko.mall.pay.api.dto.PaymentRequest;
import com.deko.mall.pay.api.dto.PaymentResultDTO;
import com.deko.mall.pay.api.dto.RefundRequestDTO;
import com.deko.mall.pay.api.dto.RefundResultDTO;
import com.deko.mall.pay.entity.PayPayment;
import com.deko.mall.pay.entity.PayPaymentChannel;
import com.deko.mall.pay.entity.PayOfflinePayment;
import com.deko.mall.pay.mapper.PayOfflinePaymentMapper;
import com.deko.mall.pay.mapper.PaymentMapper;
import com.deko.mall.pay.model.vo.OfflinePayDetailVO;
import com.deko.mall.pay.service.pay.PayService;
import com.deko.mall.pay.service.pay.PaymentChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import com.deko.mall.pay.model.dto.PayActionDTO;

/**
 * 支付服务 Feign 服务端实现（供 mall-oms 等微服务内部调用）
 */
@Slf4j
@RestController
@RequestMapping("/feign-api/pay")
@RequiredArgsConstructor
public class PayFeignController {

    private final PayService payService;
    private final PaymentMapper paymentMapper;
    private final PaymentChannelService paymentChannelService;
    private final IdGenerator idGenerator;
    private final PayOfflinePaymentMapper offlinePaymentMapper;

    /** 发起渠道支付（触发实际支付流程，如 FULL_DEDUCTION 等） */
    @PostMapping("/pay")
    public Result<PaymentResultDTO> pay(@RequestBody PaymentRequest request) {
        log.info("Feign pay: paymentNo={}, channel={}", request.getPaymentNo(), request.getPayChannel());
        PayActionDTO payDTO = new PayActionDTO();
        payDTO.setChannel(request.getPayChannel());
        payDTO.setPaymentNo(request.getPaymentNo());
        payDTO.setCode(request.getCode());
        payDTO.setDeviceType(request.getDeviceType());
        payService.paymentRequest(payDTO);
        return Result.success();
    }

    /** 创建支付记录（幂等：已存在则跳过）。paymentNo 为空则由服务端生成 */
    @PostMapping("/create")
    public Result<String> createPayment(@RequestBody PaymentRequest request) {
        String paymentNo = request.getPaymentNo();
        if (paymentNo == null || paymentNo.isEmpty()) {
            paymentNo = idGenerator.generatePayNo();
            log.info("auto-generated paymentNo: {}", paymentNo);
        } else {
            log.info("Feign createPayment: paymentNo={}, amount={}", paymentNo, request.getPayAmount());
            // 幂等检查：已存在则直接返回
            PayPayment existing = paymentMapper.selectOne(
                    new LambdaQueryWrapper<PayPayment>().eq(PayPayment::getPaymentNo, paymentNo));
            if (existing != null) {
                log.info("支付单已存在，跳过创建: paymentNo={}", paymentNo);
                return Result.success(paymentNo);
            }
        }
        PayPayment payment = new PayPayment();
        payment.setPaymentNo(paymentNo);
        payment.setMemberId(request.getMemberId());
        payment.setAmount(request.getPayAmount() != null ? request.getPayAmount() : BigDecimal.ZERO);
        payment.setPaymentContent(request.getBody());
        // 业务类型由调用方声明（支付单业务无关）；未声明时兼容为 ORDER
        payment.setBusinessType(request.getBusinessType() == null || request.getBusinessType().isBlank()
                ? "ORDER" : request.getBusinessType());
        payment.setStatus(0); // 待支付
        paymentMapper.insert(payment);
        return Result.success(paymentNo);
    }

    /** 查询支付状态（返回 PaymentResultDTO，包含 paymentChannelNo） */
    @GetMapping("/query/{paymentNo}")
    public Result<PaymentResultDTO> queryPayment(@PathVariable("paymentNo") String paymentNo) {
        PaymentResultDTO dto = new PaymentResultDTO();
        // 查询最后一条成功的渠道支付记录
        PayPaymentChannel channel = paymentChannelService.getOne(
                new LambdaQueryWrapper<PayPaymentChannel>()
                        .eq(PayPaymentChannel::getPaymentNo, paymentNo)
                        .eq(PayPaymentChannel::getStatus, 1) // 支付成功
                        .orderByDesc(PayPaymentChannel::getCreateTime)
                        .last("LIMIT 1")
        );
        if (channel != null) {
            dto.setPaymentChannelNo(channel.getPaymentChannelNo());
            dto.setChannelTradeNo(channel.getChannelTradeNo());
            dto.setChannelUserId(channel.getChannelUserId());
        }
        dto.setPaymentNo(paymentNo);
        return Result.success(dto);
    }

    /** 关闭支付 */
    @PostMapping("/close/{paymentChannelNo}")
    public Result<Boolean> closePayment(@PathVariable("paymentChannelNo") String paymentChannelNo) {
        boolean result = payService.closePayment(paymentChannelNo);
        return Result.success(result);
    }

    /** 关闭支付（按业务支付单号） */
    @PostMapping("/close-by-payment-no/{paymentNo}")
    public Result<Boolean> closePaymentByPaymentNo(@PathVariable("paymentNo") String paymentNo) {
        payService.closePaymentByPaymentNo(paymentNo);
        return Result.success(true);
    }

    /** 发起退款 */
    @PostMapping("/refund")
    public Result<RefundResultDTO> refund(@RequestBody RefundRequestDTO request) {
        log.info("Feign refund: refundNo={}, amount={}", request.getRefundNo(), request.getRefundAmount());
        payService.refundRequest(request);
        return Result.success(null);
    }

    /** 更新支付单金额 */
    @PostMapping("/update-amount")
    public Result<Boolean> updatePaymentAmount(@RequestBody PaymentRequest request) {
        boolean ok = payService.updatePaymentAmount(request);
        return Result.judge(ok);
    }

    /** 查询线下支付凭证详情 */
    @GetMapping("/offline/{paymentNo}")
    public Result<OfflinePayDetailVO> getOfflinePayDetail(
            @PathVariable("paymentNo") String paymentNo) {
        PayOfflinePayment offlinePayment = offlinePaymentMapper.selectOne(
                new LambdaQueryWrapper<PayOfflinePayment>()
                        .eq(PayOfflinePayment::getPaymentNo, paymentNo)
                        .orderByDesc(PayOfflinePayment::getCreateTime)
                        .last("LIMIT 1"));
        if (offlinePayment == null) {
            return Result.success(null);
        }
        OfflinePayDetailVO vo = new OfflinePayDetailVO();
        vo.setPaymentNo(offlinePayment.getPaymentNo());
        vo.setPaymentChannelNo(offlinePayment.getPaymentChannelNo());
        vo.setVoucherUrls(offlinePayment.getVoucherUrls());
        vo.setAuditStatus(offlinePayment.getAuditStatus());
        vo.setAuditRemark(offlinePayment.getAuditRemark());
        vo.setAuditTime(offlinePayment.getAuditTime());
        vo.setAuditor(offlinePayment.getAuditor());
        vo.setResubmitCount(offlinePayment.getResubmitCount());
        return Result.success(vo);
    }
}
