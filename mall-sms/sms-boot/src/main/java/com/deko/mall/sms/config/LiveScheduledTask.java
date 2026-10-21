package com.deko.mall.sms.config;

import com.deko.mall.sms.constant.LiveConstants;
import com.deko.mall.sms.model.vo.LiveRoomDetailVO;
import com.deko.mall.sms.service.LiveRoomService;
import com.deko.mall.sms.websocket.LiveSessionManager;
import com.deko.mall.sms.websocket.LiveWebSocketMessageBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 直播定时任务
 * <ul>
 *   <li>拉流探测 + 心跳双保险：自动判定下播（已由腾讯云回调取代，默认关闭，见 live.probe-enabled）</li>
 *   <li>live-info 定时推送：在线人数/点赞数/讲解商品实时回流前端</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LiveScheduledTask {

    private final LiveRoomService liveRoomService;
    private final LiveSessionManager sessionManager;
    private final LiveWebSocketMessageBuilder messageBuilder;
    private final LiveProperties liveProperties;

    /**
     * 每 10 秒：拉流探测 + 心跳刷新，探测连续失败自动下播
     * <p>
     * <b>默认不执行</b>：直播间状态已改由腾讯云推流/断流回调驱动（见 handleTencentCallback），
     * 而本探测的目标（拉流域名）在当前环境恒不可达 —— HTTP 超时、HTTPS 证书不匹配，
     * 会导致「直播中」约 20 秒后被误判下播（日志实测：直播间[1]拉流探测连续失败2次，自动下播）。
     * <p>
     * 保留实现与开关，是为了出问题时可把 live.probe-enabled 置 true 一键回退，且回退无需发版。
     */
    @Scheduled(fixedDelay = 10_000, initialDelay = 30_000)
    public void detectOfflineRooms() {
        if (!Boolean.TRUE.equals(liveProperties.getProbeEnabled())) {
            return;
        }
        try {
            liveRoomService.autoDetectOfflineRooms();
        } catch (Exception e) {
            log.error("直播拉流探测任务异常", e);
        }
    }

    /**
     * 每 5 秒：向有活跃会话的房间推送 live-info（在线人数/点赞/讲解商品/状态）
     */
    @Scheduled(fixedDelay = LiveConstants.WS_LIVE_INFO_PUSH_INTERVAL_SECONDS * 1000L,
            initialDelay = 15_000)
    public void pushLiveInfo() {
        for (Long roomId : sessionManager.getActiveRoomIds()) {
            try {
                LiveRoomDetailVO snapshot = liveRoomService.buildLiveInfoPush(roomId);
                if (snapshot != null) {
                    sessionManager.broadcast(roomId, messageBuilder.buildLiveInfo(snapshot));
                }
            } catch (Exception e) {
                log.warn("live-info 定时推送失败, roomId={}", roomId, e);
            }
        }
    }
}
