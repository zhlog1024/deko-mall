package com.deko.mall.oms.controller.callback;

import com.kuaidi100.sdk.response.SubscribeResp;
import com.deko.mall.oms.service.logistics.LogisticsInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 物流回调控制器
 * <p>
 * 快递100回调使用 application/x-www-form-urlencoded 格式发送:
 * param=<url-encoded-JSON>&sign=<MD5>
 */
@Tag(name = "物流回调接口")
@Slf4j
@RestController
@RequestMapping("/callback-api/v1/logistics")
@RequiredArgsConstructor
public class LogisticsCallbackController {

    private final LogisticsInfoService logisticsInfoService;

    /**
     * 快递100回调接口
     * <p>
     * 快递100以 form-urlencoded 格式 POST 回调数据，字段为 param（JSON字符串）和 sign（MD5签名）
     *
     * @param request HTTP 请求
     * @return 快递100要求的 SubscribeResp 格式
     */
    @Operation(summary = "快递100回调")
    @PostMapping("/kuaidi100")
    public SubscribeResp handleKuaidi100Callback(HttpServletRequest request) {
        SubscribeResp resp = new SubscribeResp();
        try {
            String param = request.getParameter("param");
            String sign = request.getParameter("sign");

            if (param == null || param.isEmpty()) {
                log.warn("快递100回调参数为空");
                resp.setResult(false);
                resp.setReturnCode("500");
                resp.setMessage("参数为空");
                return resp;
            }

            boolean success = logisticsInfoService.handleCallback(param, sign);
            resp.setResult(success);
            resp.setReturnCode(success ? String.valueOf(HttpServletResponse.SC_OK) : "500");
            resp.setMessage(success ? "成功" : "处理失败");
        } catch (Exception e) {
            log.error("处理快递100回调异常", e);
            resp.setResult(false);
            resp.setReturnCode("500");
            resp.setMessage("处理异常");
        }
        return resp;
    }
}
