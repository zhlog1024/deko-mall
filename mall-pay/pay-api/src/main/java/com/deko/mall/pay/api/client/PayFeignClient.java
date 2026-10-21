package com.deko.mall.pay.api.client;

import com.deko.mall.common.result.Result;
import com.deko.mall.pay.api.dto.PaymentRequest;
import com.deko.mall.pay.api.dto.PaymentResultDTO;
import com.deko.mall.pay.api.dto.RefundRequestDTO;
import com.deko.mall.pay.api.dto.RefundResultDTO;
import com.deko.mall.pay.model.vo.OfflinePayDetailVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 支付服务 Feign 客户端（供 oms 等服务调用）
 */
@FeignClient(value = "mall-pay", contextId = "pay")
public interface PayFeignClient {

    /** 发起渠道支付（触发实际的支付流程，如 FULL_DEDUCTION 等） */
    @PostMapping("/feign-api/pay/pay")
    Result<PaymentResultDTO> pay(@RequestBody PaymentRequest request);

    /** 创建支付记录（仅创建Payment实体，不发起渠道支付）。paymentNo 传空则由 pay service 自动生成 */
    @PostMapping("/feign-api/pay/create")
    Result<String> createPayment(@RequestBody PaymentRequest request);

    /** 查询支付状态（返回 paymentChannelNo 等信息） */
    @GetMapping("/feign-api/pay/query/{paymentNo}")
    Result<PaymentResultDTO> queryPayment(@PathVariable("paymentNo") String paymentNo);

    /** 关闭支付（按渠道请求号） */
    @PostMapping("/feign-api/pay/close/{paymentChannelNo}")
    Result<Boolean> closePayment(@PathVariable("paymentChannelNo") String paymentChannelNo);

    /** 关闭支付（按业务支付单号） */
    @PostMapping("/feign-api/pay/close-by-payment-no/{paymentNo}")
    Result<Boolean> closePaymentByPaymentNo(@PathVariable("paymentNo") String paymentNo);

    /** 发起退款 */
    @PostMapping("/feign-api/pay/refund")
    Result<RefundResultDTO> refund(@RequestBody RefundRequestDTO request);

    /** 更新支付单金额 */
    @PostMapping("/feign-api/pay/update-amount")
    Result<Boolean> updatePaymentAmount(@RequestBody PaymentRequest request);

    /** 查询线下支付凭证详情 */
    @GetMapping("/feign-api/pay/offline/{paymentNo}")
    Result<OfflinePayDetailVO> getOfflinePayDetail(@PathVariable("paymentNo") String paymentNo);
}
