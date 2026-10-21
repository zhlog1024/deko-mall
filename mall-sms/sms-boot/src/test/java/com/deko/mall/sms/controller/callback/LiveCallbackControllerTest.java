package com.deko.mall.sms.controller.callback;

import com.deko.mall.sms.config.LiveProperties;
import com.deko.mall.sms.model.dto.LiveCallbackDTO;
import com.deko.mall.sms.service.LiveRoomService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 腾讯云直播回调接口测试（MockMvc）
 * <p>
 * 重点覆盖：
 * <ul>
 *   <li>snake_case 报文的正确反序列化（防 @JsonNaming 失效导致 stream_id 恒为 null 的回归）</li>
 *   <li>响应状态码语义 —— 决定腾讯云是否重试，是本接口的核心契约</li>
 * </ul>
 * <p>
 * {@code integration} 标签：本类需要完整 Spring 上下文（Nacos/Redis/DB），
 * CI 用 {@code -DexcludedGroups=integration} 排除；本地全量跑（hooks/pre-push）不受影响。
 */
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class LiveCallbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LiveRoomService liveRoomService;

    @MockBean
    private LiveProperties liveProperties;

    @Test
    @DisplayName("推流回调：snake_case 字段完整反序列化，返回 200 success")
    void streamBegin_shouldDeserializeSnakeCaseAndReturnSuccess() throws Exception {
        when(liveRoomService.handleTencentCallback(any())).thenReturn(true);

        mockMvc.perform(post("/callback-api/v1/live/tencent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"t\":1700000000,\"sign\":\"abc123\",\"event_type\":1,"
                                + "\"appid\":12345678,\"app\":\"push.example.com\",\"appname\":\"live\","
                                + "\"stream_id\":\"st123\",\"channel_id\":\"st123\","
                                + "\"event_time\":1700000000,\"sequence\":\"seq-1\",\"node\":\"1.2.3.4\","
                                + "\"user_ip\":\"5.6.7.8\",\"stream_param\":\"k=v\",\"errcode\":0,\"errmsg\":\"ok\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("success"));

        ArgumentCaptor<LiveCallbackDTO> captor = ArgumentCaptor.forClass(LiveCallbackDTO.class);
        verify(liveRoomService).handleTencentCallback(captor.capture());
        LiveCallbackDTO dto = captor.getValue();
        assertEquals(Long.valueOf(1700000000L), dto.getT());
        assertEquals("abc123", dto.getSign());
        assertEquals(Integer.valueOf(1), dto.getEventType());
        assertEquals(Integer.valueOf(12345678), dto.getAppid());
        assertEquals("push.example.com", dto.getApp());
        assertEquals("live", dto.getAppname());
        assertEquals("st123", dto.getStreamId());
        assertEquals("st123", dto.getChannelId());
        assertEquals(Long.valueOf(1700000000L), dto.getEventTime());
        assertEquals("seq-1", dto.getSequence());
        assertEquals("5.6.7.8", dto.getUserIp());
        assertEquals("k=v", dto.getStreamParam());
        assertEquals(Integer.valueOf(0), dto.getErrcode());
        assertEquals("ok", dto.getErrmsg());
    }

    @Test
    @DisplayName("断流回调：push_duration 可反序列化（仅断流事件携带）")
    void streamEnd_shouldDeserializePushDuration() throws Exception {
        when(liveRoomService.handleTencentCallback(any())).thenReturn(true);

        mockMvc.perform(post("/callback-api/v1/live/tencent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"t\":1700000000,\"sign\":\"abc\",\"event_type\":0,"
                                + "\"stream_id\":\"st123\",\"push_duration\":\"128581\","
                                + "\"errcode\":1,\"errmsg\":\"The push client actively stopped the push\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("success"));

        ArgumentCaptor<LiveCallbackDTO> captor = ArgumentCaptor.forClass(LiveCallbackDTO.class);
        verify(liveRoomService).handleTencentCallback(captor.capture());
        assertEquals("128581", captor.getValue().getPushDuration());
    }

    @Test
    @DisplayName("签名/时效校验失败：返回 200 但 body 为 fail（避免无效重试）")
    void invalidSign_shouldReturn200WithFailBody() throws Exception {
        when(liveRoomService.handleTencentCallback(any())).thenReturn(false);

        mockMvc.perform(post("/callback-api/v1/live/tencent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"t\":1,\"sign\":\"bad\",\"event_type\":1,\"stream_id\":\"st123\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("fail"));
    }

    @Test
    @DisplayName("处理过程异常：返回 500 让腾讯云重试（幂等保证重复安全）")
    void handlerThrows_shouldReturn500() throws Exception {
        when(liveRoomService.handleTencentCallback(any())).thenThrow(new RuntimeException("db down"));

        mockMvc.perform(post("/callback-api/v1/live/tencent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"t\":1700000000,\"sign\":\"abc\",\"event_type\":1,\"stream_id\":\"st123\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("error"));
    }

    @Test
    @DisplayName("录制回调：未声明字段被忽略，不因 ignoreUnknown 缺失而 400")
    void recordCallback_withUnknownFields_shouldNotFail() throws Exception {
        when(liveRoomService.handleTencentCallback(any())).thenReturn(true);

        mockMvc.perform(post("/callback-api/v1/live/tencent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"t\":1700000000,\"sign\":\"abc\",\"event_type\":100,"
                                + "\"stream_id\":\"st123\",\"file_id\":\"1234567890\","
                                + "\"video_url\":\"https://vod.test.com/a.mp4\",\"file_format\":\"mp4\","
                                + "\"duration\":600,\"start_time\":1700000000,\"end_time\":1700000600,"
                                + "\"callback_ext\":\"{\\\"video_codec\\\":\\\"h264\\\"}\","
                                + "\"some_future_field\":\"whatever\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("success"));

        ArgumentCaptor<LiveCallbackDTO> captor = ArgumentCaptor.forClass(LiveCallbackDTO.class);
        verify(liveRoomService).handleTencentCallback(captor.capture());
        // 本期不消费录制字段，但 stream_id 必须能解析出来用于日志定位
        assertEquals("st123", captor.getValue().getStreamId());
        assertNull(captor.getValue().getPushDuration());
    }
}
