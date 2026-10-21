package com.deko.mall.sms.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.deko.mall.common.result.Result;
import com.deko.mall.sms.constant.LiveConstants;
import com.deko.mall.sms.service.LiveRoomService;
import com.deko.mall.ums.api.MemberFeignClient;
import com.deko.mall.ums.dto.MemberInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;

/**
 * 直播间 WebSocket 处理器
 * <p>
 * 协议（直播前端 live-content.vue 契约）：
 * 1. <b>连接建立后由服务端主动下发</b> init{fd,ping_interval}
 *    —— 前端 socket_connect 的 onOpen 不发送任何消息，只在收到本帧后才回发 init，
 *    故此处必须主动下发，否则双方互等形成死锁（聊天/点赞/在线人数全部失效）
 * 2. 客户端发送 init（带 token/live_id）→ 服务端校验 token，回 init{fd,ping_interval}
 * 3. 客户端再发 init → 服务端 joinRoom 并回 init-success{live_user_id}、广播 join
 * 4. 客户端发送 live-info → 服务端回实时快照；服务端每5秒主动推送
 * 5. message / live-like / ping
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LiveWebSocketHandler extends TextWebSocketHandler {

    private final LiveSessionManager sessionManager;
    private final LiveRoomService liveRoomService;
    private final LiveWebSocketMessageBuilder messageBuilder;
    private final JwtDecoder jwtDecoder;
    private final MemberFeignClient memberFeignClient;
    private final ObjectMapper objectMapper;

    /** session 属性 key */
    private static final String ATTR_ROOM_ID = "roomId";
    private static final String ATTR_MEMBER_ID = "memberId";
    private static final String ATTR_NICKNAME = "nickname";
    private static final String ATTR_AVATAR = "avatar";
    private static final String ATTR_LIVE_USER_ID = "liveUserId";

    /**
     * 连接建立：立即下发 init{fd,ping_interval}，触发前端后续的 init 握手。
     * <p>
     * 前端契约（live-content.vue）：onOpen 只置标志位不发消息，唯一发送 init 的位置在
     * 收到服务端 init 之后。因此服务端必须先说话，否则握手永远无法推进。
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        session.sendMessage(new TextMessage(
                messageBuilder.buildInit(session.getId(), LiveConstants.WS_PING_INTERVAL)));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            JsonNode node = objectMapper.readTree(message.getPayload());
            String type = node.path("type").asText("");
            Long roomId = node.path("live_id").asLong(0);
            switch (type) {
                case LiveConstants.WS_TYPE_INIT -> handleInit(session, node, roomId);
                case LiveConstants.WS_TYPE_PING -> { /* 心跳，无需处理 */ }
                case LiveConstants.WS_TYPE_MESSAGE -> handleMessage(session, node, roomId);
                case LiveConstants.WS_TYPE_LIVE_INFO -> handleLiveInfo(session, roomId);
                case LiveConstants.WS_TYPE_LIVE_LIKE -> handleLike(session, node, roomId);
                default -> log.debug("未识别 WS 消息类型: {}", type);
            }
        } catch (Exception e) {
            log.warn("WS 消息处理异常, session={}", session.getId(), e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Object roomIdObj = session.getAttributes().get(ATTR_ROOM_ID);
        if (roomIdObj != null) {
            sessionManager.leaveRoom((Long) roomIdObj, session);
        }
    }

    /**
     * 第一步 init：校验 token → 回 init{fd,ping_interval}；第二步 init：回 init-success 并广播 join
     */
    private void handleInit(WebSocketSession session, JsonNode node, Long roomId) throws Exception {
        // 已初始化（第二次 init，带 fd）
        if (session.getAttributes().get(ATTR_LIVE_USER_ID) != null) {
            if (roomId == null || roomId <= 0) {
                session.sendMessage(new TextMessage(messageBuilder.buildInitFail("直播间ID缺失")));
                return;
            }
            session.getAttributes().put(ATTR_ROOM_ID, roomId);
            sessionManager.joinRoom(roomId, session);
            session.sendMessage(new TextMessage(messageBuilder.buildInitSuccess(session.getId())));
            // 广播加入提示
            String nickname = (String) session.getAttributes().get(ATTR_NICKNAME);
            if (nickname != null && !nickname.isEmpty()) {
                sessionManager.broadcast(roomId, messageBuilder.buildJoin(nickname + " 加入了直播"));
            }
            return;
        }

        // 第一次 init：token 鉴权
        String token = node.path("token").asText("");
        Jwt jwt;
        try {
            jwt = jwtDecoder.decode(token);
        } catch (Exception e) {
            log.debug("WS 鉴权失败: {}", e.getMessage());
            session.sendMessage(new TextMessage(messageBuilder.buildInitFail("登录已失效，请重新登录")));
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }
        Map<String, Object> claims = jwt.getClaims();
        Long memberId = claims.get("memberId") == null ? null : Long.valueOf(String.valueOf(claims.get("memberId")));
        if (memberId == null) {
            session.sendMessage(new TextMessage(messageBuilder.buildInitFail("请先登录")));
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }
        Map<String, Object> attrs = session.getAttributes();
        attrs.put(ATTR_MEMBER_ID, memberId);
        attrs.put(ATTR_LIVE_USER_ID, session.getId());
        // 会员昵称/头像（失败兜底）
        String nickname = "用户" + memberId;
        String avatar = "";
        try {
            Result<MemberInfoDTO> memberResult = memberFeignClient.info(memberId);
            if (memberResult != null && "00000".equals(memberResult.getCode()) && memberResult.getData() != null) {
                MemberInfoDTO member = memberResult.getData();
                if (member.getNickName() != null && !member.getNickName().isEmpty()) {
                    nickname = member.getNickName();
                }
                if (member.getAvatarUrl() != null) {
                    avatar = member.getAvatarUrl();
                }
            }
        } catch (Exception e) {
            log.warn("获取会员[{}]信息失败，使用兜底昵称", memberId);
        }
        attrs.put(ATTR_NICKNAME, nickname);
        attrs.put(ATTR_AVATAR, avatar);
        session.sendMessage(new TextMessage(messageBuilder.buildInit(session.getId(), LiveConstants.WS_PING_INTERVAL)));
    }

    /**
     * 聊天消息 → 房间广播
     */
    private void handleMessage(WebSocketSession session, JsonNode node, Long roomId) throws Exception {
        Long memberId = (Long) session.getAttributes().get(ATTR_MEMBER_ID);
        if (memberId == null || roomId == null || roomId <= 0) {
            return;
        }
        // 聊天开关兜底校验：房间关闭聊天则静默丢弃（与空消息/超长消息处理一致）
        if (!liveRoomService.isChatEnabled(roomId)) {
            return;
        }
        String content = node.path("content").asText("").trim();
        if (content.isEmpty() || content.length() > 500) {
            return;
        }
        String avatar = (String) session.getAttributes().get(ATTR_AVATAR);
        String nickname = (String) session.getAttributes().get(ATTR_NICKNAME);
        sessionManager.broadcast(roomId, messageBuilder.buildMessage(content, avatar, nickname));
    }

    /**
     * live-info 请求 → 回推实时快照
     */
    private void handleLiveInfo(WebSocketSession session, Long roomId) throws Exception {
        if (roomId == null || roomId <= 0) {
            return;
        }
        pushLiveInfo(roomId);
    }

    /**
     * 点赞 → 累加 Redis 计数（点赞数经 live-info 定时推送回流前端）
     */
    private void handleLike(WebSocketSession session, JsonNode node, Long roomId) {
        Long memberId = (Long) session.getAttributes().get(ATTR_MEMBER_ID);
        if (memberId == null || roomId == null || roomId <= 0) {
            return;
        }
        int count = Math.max(1, node.path("content").asInt(1));
        liveRoomService.addLike(roomId, count);
    }

    /**
     * 推送 live-info 快照到房间
     */
    private void pushLiveInfo(Long roomId) {
        var snapshot = liveRoomService.buildLiveInfoPush(roomId);
        if (snapshot == null) {
            return;
        }
        try {
            sessionManager.broadcast(roomId, messageBuilder.buildLiveInfo(snapshot));
        } catch (Exception e) {
            log.warn("live-info 推送失败, roomId={}", roomId, e);
        }
    }
}
