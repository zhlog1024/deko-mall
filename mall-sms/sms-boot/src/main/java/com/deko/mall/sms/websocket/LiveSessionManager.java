package com.deko.mall.sms.websocket;

import com.deko.mall.sms.constant.LiveConstants;
import com.deko.mall.sms.model.vo.LiveOnlineUserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 直播间会话管理器
 * <p>
 * 按房间维度维护 WS 会话；在线人数走 Redis 计数（前端契约 online_count 来源）
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class LiveSessionManager {

    /** 房间ID → 会话集合 */
    private final Map<Long, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 加入房间
     */
    public void joinRoom(Long roomId, WebSocketSession session) {
        roomSessions.computeIfAbsent(roomId, k -> new CopyOnWriteArraySet<>()).add(session);
        redisTemplate.opsForValue().increment(LiveConstants.LIVE_ROOM_ONLINE_PREFIX + roomId);
        redisTemplate.expire(LiveConstants.LIVE_ROOM_ONLINE_PREFIX + roomId,
                LiveConstants.LIVE_ROOM_COUNT_TTL_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 离开房间
     */
    public void leaveRoom(Long roomId, WebSocketSession session) {
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                roomSessions.remove(roomId);
            }
        }
        Long online = redisTemplate.opsForValue().decrement(LiveConstants.LIVE_ROOM_ONLINE_PREFIX + roomId);
        if (online != null && online <= 0) {
            redisTemplate.delete(LiveConstants.LIVE_ROOM_ONLINE_PREFIX + roomId);
        }
    }

    /**
     * 房间在线人数
     */
    public long getOnlineCount(Long roomId) {
        Object count = redisTemplate.opsForValue().get(LiveConstants.LIVE_ROOM_ONLINE_PREFIX + roomId);
        return count == null ? 0L : Long.parseLong(String.valueOf(count));
    }

    /**
     * 获取房间所有会话
     */
    public Set<WebSocketSession> getSessions(Long roomId) {
        return roomSessions.getOrDefault(roomId, Set.of());
    }

    /**
     * 获取房间内在线观众列表（WS live-info online_user）
     * <p>
     * 必须返回<b>对象</b>而非裸字符串数组：前端模板是
     * {@code v-for="item in online_user"} + {@code <image :src="item.avatar" />}，
     * 返回 List&lt;String&gt; 会让 item.avatar 恒为 undefined，头像静默渲染为空。
     * 同时过滤空串头像（未取到会员头像时兜底值是 ""）。
     */
    public List<LiveOnlineUserVO> getOnlineUserAvatars(Long roomId) {
        return getSessions(roomId).stream()
                .map(s -> (String) s.getAttributes().get("avatar"))
                .filter(a -> a != null && !a.isBlank())
                .distinct()
                .limit(20)
                .map(LiveOnlineUserVO::new)
                .collect(Collectors.toList());
    }

    /**
     * 向房间广播消息
     */
    public void broadcast(Long roomId, String message) {
        Set<WebSocketSession> sessions = getSessions(roomId);
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    log.warn("直播间[{}]消息推送失败, session={}", roomId, session.getId(), e);
                }
            }
        }
    }

    /**
     * 获取当前有活跃会话的房间
     */
    public Set<Long> getActiveRoomIds() {
        return roomSessions.keySet();
    }
}
