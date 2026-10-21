package com.deko.mall.pay.controller.callback;

import com.deko.mall.pay.service.pay.PayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 支付回调控制器（支持 POST / GET）
 */
@Tag(name = "支付回调接口")
@RestController
@RequestMapping("/callback-api/v1/payment/notify")
@RequiredArgsConstructor
@Slf4j
public class PaymentCallbackController {

    private final PayService payService;

    @Operation(summary = "统一支付回调")
    @PostMapping("/{channel}")
    @GetMapping("/{channel}")
    public String handlePaymentCallback(@PathVariable String channel, HttpServletRequest request) {
        log.info("支付回调 渠道={} method={} query={}", channel, request.getMethod(), request.getQueryString());
        boolean callback = payService.paymentResult(channel, request);
        log.info("支付回调处理结果 渠道={} result={}", channel, callback);
        return callback ? "success" : "failure";
    }
}
