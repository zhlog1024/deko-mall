package com.deko.mall.sms.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.deko.mall.sms.constant.LiveConstants;
import com.deko.mall.sms.model.vo.LiveRoomDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * WS 消息构建器（直播前端 live-content.vue 契约）
 * <p>
 * 服务端→客户端统一结构：{code:0, data:{type, data, content}}
 */
@Component
@RequiredArgsConstructor
public class LiveWebSocketMessageBuilder {

    private final ObjectMapper objectMapper;

    /**
     * 初始化响应：{data:{fd, ping_interval}}
     */
    public String buildInit(String fd, int pingInterval) throws JsonProcessingException {
        ObjectNode data = objectMapper.createObjectNode();
        data.put("fd", fd);
        data.put("ping_interval", pingInterval);
        return build(LiveConstants.WS_TYPE_INIT, data, null);
    }

    /**
     * 初始化成功：{data:{live_user_id}}
     */
    public String buildInitSuccess(String liveUserId) throws JsonProcessingException {
        ObjectNode data = objectMapper.createObjectNode();
        data.put("live_user_id", liveUserId);
        return build(LiveConstants.WS_TYPE_INIT_SUCCESS, data, null);
    }

    /**
     * 初始化失败（鉴权失败等）
     */
    public String buildInitFail(String msg) throws JsonProcessingException {
        return build(LiveConstants.WS_TYPE_INIT_FAIL, objectMapper.createObjectNode(), msg);
    }

    /**
     * 加入直播间提示：content="xxx 加入了直播"
     */
    public String buildJoin(String content) throws JsonProcessingException {
        return build(LiveConstants.WS_TYPE_JOIN, objectMapper.createObjectNode(), content);
    }

    /**
     * 聊天消息：data:{user:{avatar, nickname}}，content=消息文本
     */
    public String buildMessage(String content, String avatar, String nickname) throws JsonProcessingException {
        ObjectNode user = objectMapper.createObjectNode();
        user.put("avatar", avatar == null ? "" : avatar);
        user.put("nickname", nickname == null ? "" : nickname);
        ObjectNode data = objectMapper.createObjectNode();
        data.set("user", user);
        return build(LiveConstants.WS_TYPE_MESSAGE, data, content);
    }

    /**
     * 直播间实时数据：data = LiveRoomDetailVO（@JsonNaming snake_case）
     */
    public String buildLiveInfo(LiveRoomDetailVO vo) throws JsonProcessingException {
        JsonNode data = objectMapper.valueToTree(vo);
        return build(LiveConstants.WS_TYPE_LIVE_INFO, data, null);
    }

    private String build(String type, JsonNode data, String content) throws JsonProcessingException {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("code", 0);
        ObjectNode body = root.putObject("data");
        body.put("type", type);
        if (content != null) {
            body.put("content", content);
        }
        body.set("data", data == null ? objectMapper.createObjectNode() : data);
        return objectMapper.writeValueAsString(root);
    }
}
