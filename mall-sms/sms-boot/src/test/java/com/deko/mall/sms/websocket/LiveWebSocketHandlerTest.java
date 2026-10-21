package com.deko.mall.sms.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.deko.mall.common.result.Result;
import com.deko.mall.sms.service.LiveRoomService;
import com.deko.mall.ums.api.MemberFeignClient;
import com.deko.mall.ums.dto.MemberInfoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 直播间 WebSocket 协议测试（对齐前端 live-content.vue 逆向契约）
 * <p>
 * 覆盖：init 两步握手（鉴权→init{fd,ping_interval}→init-success）、非法 token 拒绝、
 * 聊天消息广播（message 带 user.avatar/nickname）
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LiveWebSocketHandlerTest {

    private LiveWebSocketHandler handler;

    @Mock
    private LiveSessionManager sessionManager;

    @Mock
    private LiveRoomService liveRoomService;

    @Mock
    private JwtDecoder jwtDecoder;

    @Mock
    private MemberFeignClient memberFeignClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Map<String, Object> sessionAttrs;

    private WebSocketSession session;

    @BeforeEach
    void setUp() throws Exception {
        handler = new LiveWebSocketHandler(sessionManager, liveRoomService,
                new LiveWebSocketMessageBuilder(objectMapper), jwtDecoder, memberFeignClient, objectMapper);
        sessionAttrs = new HashMap<>();
        session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn("sock-001");
        when(session.getAttributes()).thenReturn(sessionAttrs);
        when(session.isOpen()).thenReturn(true);
    }

    private Jwt buildJwt(Long memberId) {
        return Jwt.withTokenValue("token-1")
                .header("alg", "none")
                .claim("memberId", memberId)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }

    private String payload(String type, String fd, Long liveId) {
        return "{"
                + "\"type\":\"" + type + "\","
                + "\"token\":\"token-1\","
                + "\"live_id\":" + liveId + ","
                + "\"fd\":\"" + fd + "\","
                + "\"content\":\"你好\""
                + "}";
    }

    @Test
    @DisplayName("连接建立：服务端主动下发 init{fd,ping_interval}（前端 onOpen 不发消息，必须先说话）")
    void afterConnectionEstablished_shouldSendInitFrame() throws Exception {
        handler.afterConnectionEstablished(session);

        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session).sendMessage(captor.capture());
        JsonNode msg = objectMapper.readTree(captor.getValue().getPayload());
        assertEquals(0, msg.get("code").asInt());
        assertEquals("init", msg.get("data").get("type").asText());
        assertEquals("sock-001", msg.get("data").get("data").get("fd").asText());
        assertEquals(30, msg.get("data").get("data").get("ping_interval").asInt());
    }

    @Test
    @DisplayName("完整握手链：连接→服务端init→客户端init→init成功（回归：不得死锁）")
    void fullHandshake_fromConnectionToInitSuccess() throws Exception {
        when(jwtDecoder.decode("token-1")).thenReturn(buildJwt(1L));
        MemberInfoDTO member = new MemberInfoDTO();
        member.setNickName("小张");
        member.setAvatarUrl("https://cdn.example.com/a.png");
        when(memberFeignClient.info(1L)).thenReturn(Result.success(member));

        // 1. 建连 → 服务端必须主动下发 init，否则前端不会发任何消息
        handler.afterConnectionEstablished(session);
        // 2. 前端收到 init 后回发 init（携带 token）
        handler.handleTextMessage(session, new TextMessage(payload("init", "sock-001", 1L)));
        // 3. 前端收到第二次 init 后再回发 init → 服务端完成入房
        handler.handleTextMessage(session, new TextMessage(payload("init", "sock-001", 1L)));

        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session, org.mockito.Mockito.times(3)).sendMessage(captor.capture());
        // 最后一帧必须是 init-success —— 证明整条链路走通、未死锁
        JsonNode last = objectMapper.readTree(captor.getAllValues().get(2).getPayload());
        assertEquals("init-success", last.get("data").get("type").asText());
        assertEquals("sock-001", last.get("data").get("data").get("live_user_id").asText());
        verify(sessionManager).joinRoom(1L, session);
    }

    @Test
    @DisplayName("两步握手：init→init{fd,ping_interval}，再 init→init-success+join 广播")
    void handleInit_shouldCompleteTwoStepHandshake() throws Exception {
        when(jwtDecoder.decode("token-1")).thenReturn(buildJwt(1L));
        MemberInfoDTO member = new MemberInfoDTO();
        member.setNickName("小张");
        member.setAvatarUrl("https://cdn.example.com/a.png");
        when(memberFeignClient.info(1L)).thenReturn(Result.success(member));

        // 第一步：init（无 fd）
        handler.handleTextMessage(session, new TextMessage(payload("init", "", 1L)));
        ArgumentCaptor<TextMessage> firstCaptor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session).sendMessage(firstCaptor.capture());
        JsonNode first = objectMapper.readTree(firstCaptor.getValue().getPayload());
        assertEquals(0, first.get("code").asInt());
        assertEquals("init", first.get("data").get("type").asText());
        assertEquals("sock-001", first.get("data").get("data").get("fd").asText());
        assertEquals(30, first.get("data").get("data").get("ping_interval").asInt());

        // 第二步：init（带 fd）
        handler.handleTextMessage(session, new TextMessage(payload("init", "sock-001", 1L)));
        ArgumentCaptor<TextMessage> secondCaptor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session, org.mockito.Mockito.times(2)).sendMessage(secondCaptor.capture());
        JsonNode second = objectMapper.readTree(secondCaptor.getAllValues().get(1).getPayload());
        assertEquals("init-success", second.get("data").get("type").asText());
        assertEquals("sock-001", second.get("data").get("data").get("live_user_id").asText());
        // join 广播
        verify(sessionManager).joinRoom(1L, session);
        ArgumentCaptor<String> joinCaptor = ArgumentCaptor.forClass(String.class);
        verify(sessionManager).broadcast(anyLong(), joinCaptor.capture());
        JsonNode join = objectMapper.readTree(joinCaptor.getValue());
        assertEquals("join", join.get("data").get("type").asText());
        assertTrue(join.get("data").get("content").asText().contains("小张"));
    }

    @Test
    @DisplayName("非法 token：回 init-fail 并关闭连接")
    void handleInit_withInvalidToken_shouldFailAndClose() throws Exception {
        when(jwtDecoder.decode("token-1")).thenThrow(new RuntimeException("invalid token"));

        handler.handleTextMessage(session, new TextMessage(payload("init", "", 1L)));

        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session).sendMessage(captor.capture());
        JsonNode msg = objectMapper.readTree(captor.getValue().getPayload());
        assertEquals("init-fail", msg.get("data").get("type").asText());
        verify(session).close(any());
    }

    @Test
    @DisplayName("聊天消息：广播 message（带 user.avatar/nickname 与 content）")
    void handleMessage_shouldBroadcastChat() throws Exception {
        // 模拟已完成两步握手
        sessionAttrs.put("memberId", 1L);
        sessionAttrs.put("nickname", "小张");
        sessionAttrs.put("avatar", "https://cdn.example.com/a.png");
        sessionAttrs.put("liveUserId", "sock-001");
        when(liveRoomService.isChatEnabled(1L)).thenReturn(true);

        handler.handleTextMessage(session, new TextMessage(payload("message", "sock-001", 1L)));

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(sessionManager).broadcast(anyLong(), captor.capture());
        JsonNode msg = objectMapper.readTree(captor.getValue());
        assertEquals("message", msg.get("data").get("type").asText());
        assertEquals("你好", msg.get("data").get("content").asText());
        assertEquals("小张", msg.get("data").get("data").get("user").get("nickname").asText());
        assertEquals("https://cdn.example.com/a.png", msg.get("data").get("data").get("user").get("avatar").asText());
    }

    @Test
    @DisplayName("聊天消息：房间关闭聊天时静默丢弃（WS 层兜底校验）")
    void handleMessage_whenChatDisabled_shouldNotBroadcast() throws Exception {
        // 模拟已完成两步握手
        sessionAttrs.put("memberId", 1L);
        sessionAttrs.put("nickname", "小张");
        sessionAttrs.put("avatar", "https://cdn.example.com/a.png");
        sessionAttrs.put("liveUserId", "sock-001");
        when(liveRoomService.isChatEnabled(1L)).thenReturn(false);

        handler.handleTextMessage(session, new TextMessage(payload("message", "sock-001", 1L)));

        verify(sessionManager, never()).broadcast(anyLong(), anyString());
    }
}
