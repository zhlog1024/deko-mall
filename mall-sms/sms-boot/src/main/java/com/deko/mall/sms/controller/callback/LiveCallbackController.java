package com.deko.mall.sms.controller.callback;

import com.deko.mall.sms.model.dto.LiveCallbackDTO;
import com.deko.mall.sms.service.LiveRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 腾讯云直播事件回调
 * <p>
 * 由腾讯云按事件 POST 推送（推流/断流/录制/截图），不是前端调用，
 * 故与 {@code mall-pay} 的回调一致放在 callback 包下，并加入安全白名单（见 security.whitelist-paths）。
 * <p>
 * 对外地址：{@code https://mall-api.example.com/mall-sms/callback-api/v1/live/tencent}
 * （经网关 StripPrefix=1 后到达本服务的 {@code /callback-api/v1/live/tencent}）
 * <p>
 * 报文与签名规则见官方文档：
 * https://www.tencentcloud.com/zh/document/product/267/38081
 */
@Tag(name = "腾讯云直播回调")
@RestController
@RequestMapping("/callback-api/v1/live")
@RequiredArgsConstructor
@Slf4j
public class LiveCallbackController {

    private final LiveRoomService liveRoomService;

    /**
     * 腾讯云直播事件回调
     * <p>
     * 响应状态码语义（决定腾讯云是否重试）：
     * <ul>
     *   <li>200 success —— 受理成功，含幂等跳过、无关事件、未匹配到房间</li>
     *   <li>200 fail —— 签名或时效校验未通过。非腾讯云来源，重试无意义，故仍返回 200 避免重试风暴</li>
     *   <li>500 error —— 处理过程异常（可能是瞬时故障），返回非 200 让腾讯云重试；
     *       处理逻辑已做幂等，重复投递安全</li>
     * </ul>
     */
    @Operation(summary = "腾讯云直播事件回调")
    @PostMapping("/tencent")
    public ResponseEntity<String> tencent(@RequestBody LiveCallbackDTO dto) {
        try {
            boolean handled = liveRoomService.handleTencentCallback(dto);
            return ResponseEntity.ok(handled ? "success" : "fail");
        } catch (Exception e) {
            log.error("腾讯云直播回调处理异常 eventType={} stream={}", dto.getEventType(), dto.getStreamId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
        }
    }
}
