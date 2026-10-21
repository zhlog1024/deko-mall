package com.deko.mall.pay.controller.callback;

import com.deko.mall.pay.api.dto.RefundResultDTO;
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
 * 退款回调控制器（支持 POST / GET）
 */
@Tag(name = "退款回调接口")
@RestController
@RequestMapping("/callback-api/v1/refund/notify")
@RequiredArgsConstructor
@Slf4j
public class RefundCallbackController {

    private final PayService payService;

    @Operation(summary = "统一退款回调")
    @PostMapping("/{channel}")
    @GetMapping("/{channel}")
    public String handleRefundCallback(@PathVariable String channel, HttpServletRequest request) {
        log.info("退款回调 渠道={} method={} query={}", channel, request.getMethod(), request.getQueryString());
        RefundResultDTO result = payService.refundCallback(channel, request);
        log.info("退款回调处理结果 渠道={} result={}", channel, result != null ? result.getStatus() : null);
        return result != null && result.getStatus() != null ? "success" : "failure";
    }
}
