package com.deko.mall.sms.controller.admin;

import com.deko.mall.common.result.PageResult;
import com.deko.mall.sms.config.LiveProperties;
import com.deko.mall.sms.model.form.LiveRoomForm;
import com.deko.mall.sms.model.form.LiveRoomGoodsForm;
import com.deko.mall.sms.model.query.LiveRoomPageQuery;
import com.deko.mall.sms.model.vo.LiveRoomDetailVO;
import com.deko.mall.sms.model.vo.LiveRoomPageVO;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 直播管理接口契约测试（MockMvc）
 * <p>
 * 覆盖：新增字段映射（防"属性没保存"回归）、设置接口返回 VO（防 @RefreshScope 代理序列化 400）、
 * 编辑/状态/分页接口契约
 * <p>
 * {@code integration} 标签：本类需要完整 Spring 上下文（Nacos/Redis/DB），
 * CI 用 {@code -DexcludedGroups=integration} 排除；本地全量跑（hooks/pre-push）不受影响。
 */
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class SmsLiveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LiveRoomService liveRoomService;

    @MockBean
    private LiveProperties liveProperties;

    @Test
    @DisplayName("新增直播间：表单字段完整映射到 service（防属性丢失回归）")
    void createRoom_shouldMapAllFormFields() throws Exception {
        when(liveRoomService.createRoom(any())).thenReturn(100L);

        mockMvc.perform(post("/api/v1/live/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"测试直播间\",\"cover\":\"https://cdn.test.com/cover.png\","
                                + "\"anchorName\":\"主播甲\",\"anchorAvatar\":\"https://cdn.test.com/avatar.png\","
                                + "\"isLiveChat\":1,\"isLiveGoodsBuy\":0,\"isLiveLike\":1,"
                                + "\"liveTips\":\"欢迎来到直播间\",\"liveReconnectNumber\":5,"
                                + "\"liveReconnectIntervalTime\":4,\"liveGoodsExplainAutoCloseTime\":15,"
                                + "\"sort\":9,\"remark\":\"备注\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data").value(100));

        ArgumentCaptor<LiveRoomForm> captor = ArgumentCaptor.forClass(LiveRoomForm.class);
        verify(liveRoomService).createRoom(captor.capture());
        LiveRoomForm form = captor.getValue();
        assertEquals("测试直播间", form.getTitle());
        assertEquals("https://cdn.test.com/cover.png", form.getCover());
        assertEquals("主播甲", form.getAnchorName());
        assertEquals("https://cdn.test.com/avatar.png", form.getAnchorAvatar());
        assertEquals(Integer.valueOf(1), form.getIsLiveChat());
        assertEquals(Integer.valueOf(0), form.getIsLiveGoodsBuy());
        assertEquals(Integer.valueOf(1), form.getIsLiveLike());
        assertEquals("欢迎来到直播间", form.getLiveTips());
        assertEquals(Integer.valueOf(5), form.getLiveReconnectNumber());
        assertEquals(Integer.valueOf(4), form.getLiveReconnectIntervalTime());
        assertEquals(Integer.valueOf(15), form.getLiveGoodsExplainAutoCloseTime());
        assertEquals(Integer.valueOf(9), form.getSort());
        assertEquals("备注", form.getRemark());
    }

    @Test
    @DisplayName("全局设置：返回 LiveSettingsVO 字段（防 @RefreshScope 代理序列化 400 回归）")
    void getSettings_shouldReturnVoFields() throws Exception {
        when(liveProperties.getTencentSecretId()).thenReturn("AKID-test");
        when(liveProperties.getPushDomain()).thenReturn("push.example.com");
        when(liveProperties.getPlayDomain()).thenReturn("play.example.com");
        when(liveProperties.getPlayUseHttps()).thenReturn(true);
        when(liveProperties.getWsHost()).thenReturn("127.0.0.1");
        when(liveProperties.getWsPort()).thenReturn(8804);
        when(liveProperties.getWsIsWss()).thenReturn(0);
        when(liveProperties.getApplicationName()).thenReturn("健康公社");

        mockMvc.perform(get("/api/v1/live/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.tencentSecretId").value("AKID-test"))
                .andExpect(jsonPath("$.data.pushDomain").value("push.example.com"))
                .andExpect(jsonPath("$.data.playDomain").value("play.example.com"))
                .andExpect(jsonPath("$.data.playUseHttps").value(true))
                .andExpect(jsonPath("$.data.wsHost").value("127.0.0.1"))
                .andExpect(jsonPath("$.data.wsPort").value(8804))
                .andExpect(jsonPath("$.data.wsIsWss").value(0))
                .andExpect(jsonPath("$.data.applicationName").value("健康公社"));
    }

    @Test
    @DisplayName("编辑直播间：调用 service.updateRoom")
    void updateRoom_shouldCallService() throws Exception {
        mockMvc.perform(put("/api/v1/live/rooms/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"新标题\",\"anchorName\":\"新主播\",\"isLiveChat\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));

        ArgumentCaptor<LiveRoomForm> captor = ArgumentCaptor.forClass(LiveRoomForm.class);
        verify(liveRoomService).updateRoom(captor.capture());
        assertEquals(Long.valueOf(5), captor.getValue().getId());
        assertEquals("新标题", captor.getValue().getTitle());
    }

    @Test
    @DisplayName("状态流转：调用 service.changeStatus")
    void changeStatus_shouldCallService() throws Exception {
        mockMvc.perform(put("/api/v1/live/rooms/5/status").param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));

        verify(liveRoomService).changeStatus(eq(5L), eq(1));
    }

    @Test
    @DisplayName("直播间分页：返回 data.list 结构")
    void getRoomPage_shouldReturnList() throws Exception {
        LiveRoomPageVO vo = new LiveRoomPageVO();
        vo.setId(1L);
        vo.setTitle("测试");
        vo.setStatus(0);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<LiveRoomPageVO> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
        page.setRecords(List.of(vo));
        page.setTotal(1);
        when(liveRoomService.getRoomPage(any(LiveRoomPageQuery.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/live/rooms/page").param("pageNum", "1").param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.list[0].title").value("测试"))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @DisplayName("绑定商品：调用 service.bindGoods 并透传 spuId")
    void bindGoods_shouldCallService() throws Exception {
        mockMvc.perform(post("/api/v1/live/rooms/goods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roomId\":5,\"spuId\":88,\"sort\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));

        ArgumentCaptor<LiveRoomGoodsForm> captor = ArgumentCaptor.forClass(LiveRoomGoodsForm.class);
        verify(liveRoomService).bindGoods(captor.capture());
        assertEquals(Long.valueOf(5), captor.getValue().getRoomId());
        assertEquals(Long.valueOf(88), captor.getValue().getSpuId());
    }

    @Test
    @DisplayName("直播间详情（管理端）：service 返回 VO 透传")
    void getRoomDetail_shouldReturnVo() throws Exception {
        LiveRoomPageVO vo = new LiveRoomPageVO();
        vo.setId(5L);
        vo.setTitle("详情测试");
        vo.setAnchorName("主播");
        vo.setAnchorAvatar("https://cdn.test.com/avatar.png");
        vo.setIsLiveChat(1);
        when(liveRoomService.getAdminDetail(5L)).thenReturn(vo);

        mockMvc.perform(get("/api/v1/live/rooms/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.anchorAvatar").value("https://cdn.test.com/avatar.png"))
                .andExpect(jsonPath("$.data.isLiveChat").value(1));
    }
}
