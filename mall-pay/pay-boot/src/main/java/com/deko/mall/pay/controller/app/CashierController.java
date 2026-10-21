package com.deko.mall.pay.controller.app;

import cn.hutool.json.JSONUtil;
import com.deko.mall.common.result.Result;
import com.deko.mall.pay.api.enums.PayChannelEnum;
import com.deko.mall.pay.model.form.PayForm;
import com.deko.mall.pay.model.vo.CashierVO;
import com.deko.mall.pay.model.vo.PaymentResponseVO;
import com.deko.mall.pay.service.pay.PayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import com.deko.mall.pay.model.dto.PayActionDTO;

/**
 * 收银台控制器
 */
@Tag(name = "收银台接口")
@RestController
@RequestMapping("/app-api/v1/cashier")
@RequiredArgsConstructor
public class CashierController {

    private final PayService payService;

    @Operation(summary = "获取收银台信息")
    @GetMapping
    public Result<CashierVO> getCashierInfo(
            @Parameter(description = "支付单号") @RequestParam String paymentNo,
            @Parameter(description = "设备类型") @RequestParam String deviceType) {
        CashierVO vo = payService.getCashierInfo(paymentNo, deviceType);
        return Result.success(vo);
    }

    @Operation(summary = "查询支付状态（供前端从外部支付页返回后轮询）")
    @GetMapping("/status")
    public Result<PaymentResponseVO> getPaymentStatus(
            @Parameter(description = "支付单号") @RequestParam String paymentNo) {
        PaymentResponseVO vo = payService.getPaymentStatus(paymentNo);
        return Result.success(vo);
    }

    @Operation(summary = "创建支付单")
    @PostMapping("/pay")
    public Result<PaymentResponseVO> pay(@Validated @RequestBody PayForm form) {
        // 线下支付：将凭证URL序列化后通过code参数传递
        String code = form.getCode() == null ? "" : form.getCode();
        if (PayChannelEnum.OFFLINE_PAY.getValue().equals(form.getPayChannel())
                && form.getVoucherUrls() != null && !form.getVoucherUrls().isEmpty()) {
            code = JSONUtil.toJsonStr(Map.of("voucherUrls", form.getVoucherUrls()));
        }
        PayActionDTO payDTO = new PayActionDTO();
        payDTO.setChannel(form.getPayChannel());
        payDTO.setPaymentNo(form.getPaymentNo());
        payDTO.setCode(code);
        payDTO.setDeviceType(form.getDeviceType());
        PaymentResponseVO vo = payService.paymentRequest(payDTO);
        return Result.success(vo);
    }
}
