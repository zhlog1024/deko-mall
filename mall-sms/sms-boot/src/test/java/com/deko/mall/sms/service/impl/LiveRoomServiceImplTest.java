package com.deko.mall.sms.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.deko.mall.pms.api.SpuFeignClient;
import com.deko.mall.sms.config.LiveProperties;
import com.deko.mall.sms.converter.LiveConverter;
import com.deko.mall.sms.enums.LiveCallbackEventEnum;
import com.deko.mall.sms.enums.LiveRoomStatusEnum;
import com.deko.mall.sms.mapper.LiveRoomMapper;
import com.deko.mall.sms.model.entity.SmsLiveRoom;
import com.deko.mall.sms.model.vo.LiveRoomDetailVO;
import com.deko.mall.sms.model.vo.LiveRoomListDataVO;
import com.deko.mall.sms.model.vo.LiveRoomListVO;
import com.deko.mall.sms.service.LiveRoomGoodsService;
import com.deko.mall.sms.util.LiveStreamUrlGenerator;
import com.deko.mall.sms.websocket.LiveSessionManager;
import com.deko.mall.sms.websocket.LiveWebSocketMessageBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 直播间服务测试
 * <p>
 * 覆盖：列表状态筛选与封禁排除、详情组装、开播/下播状态机
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LiveRoomServiceImplTest {

    private LiveRoomServiceImpl liveRoomService;

    @Mock
    private LiveRoomMapper liveRoomMapper;

    @Mock
    private LiveConverter liveConverter;

    @Mock
    private LiveRoomGoodsService liveRoomGoodsService;

    @Mock
    private LiveStreamUrlGenerator streamUrlGenerator;

    @Mock
    private LiveProperties liveProperties;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private LiveSessionManager sessionManager;

    @Mock
    private LiveWebSocketMessageBuilder messageBuilder;

    @Mock
    private SpuFeignClient spuFeignClient;

    @Mock
    private com.deko.mall.sms.service.AdvertService advertService;

    @BeforeEach
    void setUp() {
        // 纯单元测试初始化 MyBatis-Plus lambda 缓存（否则 SmsLiveRoom::getXxx 方法引用报错）
        com.baomidou.mybatisplus.core.metadata.TableInfoHelper.initTableInfo(
                new org.apache.ibatis.builder.MapperBuilderAssistant(
                        new com.baomidou.mybatisplus.core.MybatisConfiguration(), ""),
                SmsLiveRoom.class);
        liveRoomService = Mockito.spy(new LiveRoomServiceImpl(liveConverter, liveRoomGoodsService,
                streamUrlGenerator, liveProperties, redisTemplate, sessionManager, spuFeignClient, messageBuilder,
                advertService));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        // 桩：写库方法（单测边界，真实 DB 交互不在此验证）
        doReturn(true).when(liveRoomService).updateById(any());
        doReturn(true).when(liveRoomService).update(any());
        // 桩：getById 兜底（getOnlineCount 走 Redis 未命中时回退 DB；特定测试按需覆盖）
        doReturn(null).when(liveRoomService).getById(anyLong());
        // 桩：转换器（各测试按需覆盖）
        when(liveConverter.entity2InfoVo(any())).thenReturn(new com.deko.mall.sms.model.vo.LiveRoomInfoVO());
    }

    private SmsLiveRoom buildRoom(Long id, Integer status) {
        SmsLiveRoom room = new SmsLiveRoom();
        room.setId(id);
        room.setRoomNo("L20260101" + id);
        room.setTitle("测试直播间" + id);
        room.setCover("https://cdn.example.com/cover" + id + ".png");
        room.setStreamName("st" + id);
        room.setStatus(status);
        room.setAnchorName("主播" + id);
        room.setIsLiveChat(1);
        room.setIsLiveGoodsBuy(1);
        room.setIsLiveLike(1);
        room.setLiveReconnectNumber(3);
        room.setLiveReconnectIntervalTime(3);
        room.setLiveGoodsExplainAutoCloseTime(10);
        return room;
    }

    @Test
    @DisplayName("列表：直播中筛选 + 封禁房间排除")
    void getRoomList_shouldFilterStatusAndExcludeBanned() {
        List<SmsLiveRoom> rooms = List.of(
                buildRoom(1L, LiveRoomStatusEnum.LIVING.getValue()),
                buildRoom(2L, LiveRoomStatusEnum.BANNED.getValue()),
                buildRoom(3L, LiveRoomStatusEnum.NOT_STARTED.getValue())
        );
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SmsLiveRoom> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 20);
        page.setRecords(rooms);
        page.setTotal(rooms.size());
        doReturn(page).when(liveRoomService).page(any(com.baomidou.mybatisplus.extension.plugins.pagination.Page.class),
                any(Wrapper.class));

        LiveRoomListVO vo1 = new LiveRoomListVO();
        vo1.setId(1L);
        LiveRoomListVO vo2 = new LiveRoomListVO();
        vo2.setId(2L);
        LiveRoomListVO vo3 = new LiveRoomListVO();
        vo3.setId(3L);
        when(liveConverter.entity2ListVo(anyList())).thenReturn(List.of(vo1, vo2, vo3));

        LiveRoomListDataVO result = liveRoomService.getRoomList("1", null, null, 1, 20);

        // 查询条件：按状态筛选 + 排除封禁
        ArgumentCaptor<com.baomidou.mybatisplus.core.conditions.AbstractWrapper> wrapperCaptor =
                ArgumentCaptor.forClass(com.baomidou.mybatisplus.core.conditions.AbstractWrapper.class);
        verify(liveRoomService).page(any(com.baomidou.mybatisplus.extension.plugins.pagination.Page.class),
                wrapperCaptor.capture());
        String sqlSegment = wrapperCaptor.getValue().getExpression().getNormal().getSqlSegment();
        assertTrue(sqlSegment.contains("status = "), "应按状态筛选: " + sqlSegment);
        assertTrue(sqlSegment.contains("status <> "), "应排除封禁直播间: " + sqlSegment);
        assertTrue(wrapperCaptor.getValue().getParamNameValuePairs().containsValue(3),
                "封禁状态值应出现在查询参数中");
        // 列表项跳转地址
        assertNotNull(result.getDataList());
        assertEquals(3, result.getDataList().size());
        assertTrue(result.getDataList().stream()
                .allMatch(v -> v.getUrl().startsWith("/pages/plugins/live/detail/detail?id=")));
    }

    @Test
    @DisplayName("初始化数据：返回排序依据 search_order_by_list（含且仅含一个默认选中）")
    void getInitData_shouldExposeOrderByList() {
        com.deko.mall.sms.model.vo.LiveIndexInitVO init = liveRoomService.getInitData();

        assertNotNull(init.getSearchOrderByList(), "search_order_by_list 不能为空（前端排序弹窗依赖）");
        assertEquals(List.of("default", "hot", "new"), init.getSearchOrderByList().stream()
                .map(com.deko.mall.sms.model.vo.LiveOrderByVO::getValue).toList());
        List<com.deko.mall.sms.model.vo.LiveOrderByVO> checked = init.getSearchOrderByList().stream()
                .filter(o -> Boolean.TRUE.equals(o.getChecked())).toList();
        assertEquals(1, checked.size(), "应有且仅有一个默认选中项");
        assertEquals("default", checked.get(0).getValue());
    }

    @Test
    @DisplayName("初始化数据：slider_list 复用广告位并映射为 slider 组件契约字段名")
    void getInitData_shouldMapBannersToSliderContract() {
        com.deko.mall.sms.model.vo.BannerVO banner = new com.deko.mall.sms.model.vo.BannerVO();
        banner.setTitle("活动");
        banner.setImageUrl("https://cdn.example.com/b.png");
        banner.setRedirectUrl("/pages/activity/index");
        when(advertService.getBannerList()).thenReturn(List.of(banner));

        com.deko.mall.sms.model.vo.LiveIndexInitVO init = liveRoomService.getInitData();

        assertEquals(1, init.getSliderList().size());
        com.deko.mall.sms.model.vo.LiveSliderVO slider = init.getSliderList().get(0);
        // 组件读的是 images_url / event_value / event_type，若直接用 BannerVO 会取不到图片
        assertEquals("https://cdn.example.com/b.png", slider.getImagesUrl());
        assertEquals("/pages/activity/index", slider.getEventValue());
        assertEquals(1, slider.getEventType());
    }

    @Test
    @DisplayName("初始化数据：广告位异常时轮播降级为空，不影响排序依据等其它字段")
    void getInitData_whenAdvertFails_shouldDegradeToEmptySlider() {
        when(advertService.getBannerList()).thenThrow(new RuntimeException("advert down"));

        com.deko.mall.sms.model.vo.LiveIndexInitVO init = liveRoomService.getInitData();

        assertTrue(init.getSliderList().isEmpty(), "广告位异常时应降级为空列表");
        assertNotNull(init.getSearchOrderByList(), "降级不应影响排序依据");
        assertNotNull(init.getLiveStatusTabList(), "降级不应影响状态 Tab");
    }

    @Test
    @DisplayName("列表排序：by=hot 按点赞、new 按创建时间、缺省与 default 按 sort")
    void getRoomList_shouldApplyOrderBy() {
        assertEquals("like_count", orderByColumnOf("hot"));
        assertEquals("create_time", orderByColumnOf("new"));
        assertEquals("sort", orderByColumnOf("default"));
        assertEquals("sort", orderByColumnOf(null), "by 缺省应等同 default");
    }

    /**
     * 取 getRoomList 实际下发的 ORDER BY 首列，用于校验 by 参数生效
     */
    private String orderByColumnOf(String by) {
        doReturn(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<SmsLiveRoom>())
                .when(liveRoomService).page(any(com.baomidou.mybatisplus.extension.plugins.pagination.Page.class),
                        any(Wrapper.class));
        when(liveConverter.entity2ListVo(anyList())).thenReturn(List.of());
        Mockito.clearInvocations(liveRoomService);

        liveRoomService.getRoomList(null, null, by, 1, 20);

        ArgumentCaptor<com.baomidou.mybatisplus.core.conditions.AbstractWrapper> captor =
                ArgumentCaptor.forClass(com.baomidou.mybatisplus.core.conditions.AbstractWrapper.class);
        verify(liveRoomService).page(any(com.baomidou.mybatisplus.extension.plugins.pagination.Page.class),
                captor.capture());
        String segment = captor.getValue().getSqlSegment();
        int idx = segment.indexOf("ORDER BY");
        assertTrue(idx >= 0, "SQL 应包含 ORDER BY，实际: " + segment);
        return segment.substring(idx + "ORDER BY".length()).trim().split("[ ,]")[0];
    }

    @Test
    @DisplayName("详情：组装 live_info + config（含 socket_connect）")
    void getRoomDetail_shouldAssembleDetail() {
        SmsLiveRoom room = buildRoom(1L, LiveRoomStatusEnum.LIVING.getValue());
        doReturn(room).when(liveRoomService).getById(1L);
        com.deko.mall.sms.model.vo.LiveRoomInfoVO info = new com.deko.mall.sms.model.vo.LiveRoomInfoVO();
        info.setId(1L);
        info.setStatus(LiveRoomStatusEnum.LIVING.getValue());
        when(liveConverter.entity2InfoVo(any())).thenReturn(info);
        when(liveProperties.getWsHost()).thenReturn("127.0.0.1");
        when(liveProperties.getWsPort()).thenReturn(8804);
        when(liveProperties.getWsIsWss()).thenReturn(0);
        when(liveProperties.getWsPath()).thenReturn("/");

        LiveRoomDetailVO detail = liveRoomService.getRoomDetail(1L);

        assertNotNull(detail);
        // 回归：顶层 id 必须下发 —— 前端 live_data.id 用于 WS init 的 live_id
        // （缺失会被服务端判为「直播间ID缺失」，导致永远无法入房、收不到任何聊天广播）
        // 以及商品橱窗 propLiveId（缺失会请求 /rooms/undefined/goods）
        assertEquals(1L, detail.getId(), "LiveRoomDetailVO 顶层 id 不能为空");
        assertNotNull(detail.getLiveInfo());
        assertEquals(1L, detail.getLiveInfo().getId());
        assertEquals(LiveRoomStatusEnum.LIVING.getValue(), detail.getLiveInfo().getStatus());
        assertNotNull(detail.getConfig());
        assertEquals(1, detail.getConfig().getIsLiveChat());
        assertNotNull(detail.getConfig().getSocketConnect());
        assertEquals("127.0.0.1", detail.getConfig().getSocketConnect().getHost());
        assertEquals(8804, detail.getConfig().getSocketConnect().getPort());
        assertEquals(0, detail.getConfig().getSocketConnect().getIsWss());
        // 回归：path 必须下发 —— 生产经统一网关时为 /mall-sms/，
        // 缺失会让前端拼出无路径的地址打在网关 404（与默认值 / 语义不同，不能省）
        assertEquals("/", detail.getConfig().getSocketConnect().getPath());
        // 回归：该字段必须下发，否则前端 setInterval 周期为 NaN(≈0) 会疯狂空转
        assertEquals(30, detail.getConfig().getLiveInfoTimingIntervalTime(),
                "live_info_timing_interval_time 不能为空，且需为正整数秒");
        // 注：点赞飞心图标已改为前端用 get_static_url('live', true) 自行计算，
        // 后端不再下发 like_icon_list，故此处不再断言该字段。
    }

    @Test
    @DisplayName("开播：状态置1、记录开播时间、写心跳")
    void changeStatus_toLiving_shouldSetStartTimeAndHeartbeat() {
        SmsLiveRoom room = buildRoom(1L, LiveRoomStatusEnum.NOT_STARTED.getValue());
        doReturn(room).when(liveRoomService).getById(1L);

        liveRoomService.changeStatus(1L, LiveRoomStatusEnum.LIVING.getValue());

        assertEquals(LiveRoomStatusEnum.LIVING.getValue(), room.getStatus());
        assertNotNull(room.getLiveStartTime());
        assertNull(room.getLiveEndTime());
        verify(liveRoomService).updateById(room);
        verify(valueOperations).set(anyString(), any(), anyLong(), any());
    }

    @Test
    @DisplayName("下播：状态置0、记录下播时间、清理 Redis 计数与心跳")
    void changeStatus_toNotStarted_shouldPersistAndCleanRedis() {
        SmsLiveRoom room = buildRoom(1L, LiveRoomStatusEnum.LIVING.getValue());
        room.setLiveStartTime(LocalDateTime.now().minusHours(1));
        doReturn(room).when(liveRoomService).getById(1L);

        liveRoomService.changeStatus(1L, LiveRoomStatusEnum.NOT_STARTED.getValue());

        assertEquals(LiveRoomStatusEnum.NOT_STARTED.getValue(), room.getStatus());
        assertNotNull(room.getLiveEndTime());
        verify(liveRoomService).updateById(room);
        // 下播清理三个 Redis Key（在线人数/点赞数/状态心跳）
        verify(redisTemplate, org.mockito.Mockito.times(3)).delete(anyString());
    }

    @Test
    @DisplayName("装修列表：状态支持多选（逗号串→IN），非法值与封禁态被忽略")
    void getDiyRoomList_shouldSupportMultiStatus() {
        doReturn(List.of()).when(liveRoomService).list((Wrapper<SmsLiveRoom>) any());
        when(liveConverter.entity2ListVo(anyList())).thenReturn(List.of());

        // "0,1,abc,3," → 有效值 0/1；abc 非法被忽略；3(封禁) 被剔除（封禁本就不对外展示）
        liveRoomService.getDiyRoomList(null, "0,1,abc,3,", 6, null);

        ArgumentCaptor<com.baomidou.mybatisplus.core.conditions.AbstractWrapper> captor =
                ArgumentCaptor.forClass(com.baomidou.mybatisplus.core.conditions.AbstractWrapper.class);
        verify(liveRoomService).list(captor.capture());
        com.baomidou.mybatisplus.core.conditions.AbstractWrapper wrapper = captor.getValue();
        String sqlSegment = wrapper.getExpression().getNormal().getSqlSegment();
        java.util.Collection<Object> values = wrapper.getParamNameValuePairs().values();

        assertTrue(sqlSegment.contains("IN"), "多选状态应生成 IN 条件: " + sqlSegment);
        assertTrue(values.contains(0) && values.contains(1), "应包含勾选的状态 0/1: " + values);
        // 仅 ne(封禁) + in(0,1) 两组参数，说明 3 与 abc 未进入 IN 列表
        assertEquals(3, values.size(), "参数应仅含 封禁排除 + 0/1 两个状态: " + values);
    }

    @Test
    @DisplayName("装修列表：状态为空串/非法值时不做状态筛选")
    void getDiyRoomList_shouldIgnoreBlankAndIllegalStatus() {
        doReturn(List.of()).when(liveRoomService).list((Wrapper<SmsLiveRoom>) any());
        when(liveConverter.entity2ListVo(anyList())).thenReturn(List.of());

        liveRoomService.getDiyRoomList(null, " ,abc, ", 6, null);

        ArgumentCaptor<com.baomidou.mybatisplus.core.conditions.AbstractWrapper> captor =
                ArgumentCaptor.forClass(com.baomidou.mybatisplus.core.conditions.AbstractWrapper.class);
        verify(liveRoomService).list(captor.capture());
        String sqlSegment = captor.getValue().getExpression().getNormal().getSqlSegment();

        assertFalse(sqlSegment.contains("IN"), "无有效状态时不应生成 IN 条件: " + sqlSegment);
    }

    @Test
    @DisplayName("装修列表：返回数组、排除封禁、按数量截断")
    void getDiyRoomList_shouldReturnArrayAndLimit() {
        List<SmsLiveRoom> rooms = List.of(
                buildRoom(1L, LiveRoomStatusEnum.LIVING.getValue()),
                buildRoom(2L, LiveRoomStatusEnum.NOT_STARTED.getValue())
        );
        doReturn(rooms).when(liveRoomService).list((Wrapper<SmsLiveRoom>) any());
        LiveRoomListVO vo1 = new LiveRoomListVO();
        vo1.setId(1L);
        LiveRoomListVO vo2 = new LiveRoomListVO();
        vo2.setId(2L);
        when(liveConverter.entity2ListVo(anyList())).thenReturn(List.of(vo1, vo2));

        List<LiveRoomListVO> result = liveRoomService.getDiyRoomList(null, null, 2, null);

        ArgumentCaptor<com.baomidou.mybatisplus.core.conditions.AbstractWrapper> wrapperCaptor =
                ArgumentCaptor.forClass(com.baomidou.mybatisplus.core.conditions.AbstractWrapper.class);
        verify(liveRoomService).list(wrapperCaptor.capture());
        String sqlSegment = wrapperCaptor.getValue().getExpression().getNormal().getSqlSegment();
        assertTrue(sqlSegment.contains("status <> "), "应排除封禁直播间: " + sqlSegment);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(v -> v.getUrl().startsWith("/pages/plugins/live/detail/detail?id=")));
    }

    @Test
    @DisplayName("新增直播间：roomNo 用 snowflake 后6位，避免同日唯一键冲突")
    void createRoom_shouldUseSnowflakeTailForRoomNo() {
        com.deko.mall.sms.model.form.LiveRoomForm form = new com.deko.mall.sms.model.form.LiveRoomForm();
        form.setTitle("新增测试");
        form.setAnchorName("主播");
        when(streamUrlGenerator.generatePushUrl(anyString())).thenReturn("rtmp://push.example.com/live/st1");
        when(streamUrlGenerator.generatePullUrl(anyString())).thenReturn("https://play.example.com/live/st1");
        doReturn(0L).when(liveRoomService).count(any()); // checkStreamNameUnique 流名唯一校验
        doReturn(true).when(liveRoomService).save(any(SmsLiveRoom.class));

        liveRoomService.createRoom(form);

        ArgumentCaptor<SmsLiveRoom> captor = ArgumentCaptor.forClass(SmsLiveRoom.class);
        verify(liveRoomService).save(captor.capture());
        SmsLiveRoom saved = captor.getValue();
        assertNotNull(saved.getRoomNo());
        // 格式：L + yyyyMMdd + 6位数字（snowflake 尾 6 位，而非前 6 位时间戳高位）
        assertTrue(saved.getRoomNo().matches("^L\\d{8}\\d{6}$"), "roomNo 格式异常: " + saved.getRoomNo());
        assertTrue(saved.getStreamName().startsWith("st"), "流名应自动生成");
        assertEquals(1, saved.getIsLiveChat());
        assertEquals(1, saved.getIsLiveGoodsBuy());
        assertEquals(1, saved.getIsLiveLike());
    }

    @Test
    @DisplayName("聊天开关：开启的房间返回 true")
    void isChatEnabled_shouldReturnTrueWhenChatOpen() {
        SmsLiveRoom room = buildRoom(1L, LiveRoomStatusEnum.LIVING.getValue());
        room.setIsLiveChat(1);
        doReturn(room).when(liveRoomService).getById(1L);

        assertTrue(liveRoomService.isChatEnabled(1L));
    }

    @Test
    @DisplayName("聊天开关：关闭的房间返回 false（WS 层静默丢弃）")
    void isChatEnabled_shouldReturnFalseWhenChatClosed() {
        SmsLiveRoom room = buildRoom(1L, LiveRoomStatusEnum.LIVING.getValue());
        room.setIsLiveChat(0);
        doReturn(room).when(liveRoomService).getById(1L);

        assertFalse(liveRoomService.isChatEnabled(1L));
    }

    @Test
    @DisplayName("聊天开关：房间不存在返回 false")
    void isChatEnabled_shouldReturnFalseWhenRoomNotFound() {
        // setUp 默认桩 getById 返回 null
        assertFalse(liveRoomService.isChatEnabled(999L));
    }

    // 腾讯云直播回调

    /**
     * 构造回调报文；streamName 为空表示腾讯云未下发 stream_id/channel_id
     */
    private com.deko.mall.sms.model.dto.LiveCallbackDTO buildCallback(Integer eventType, String streamName) {
        com.deko.mall.sms.model.dto.LiveCallbackDTO dto = new com.deko.mall.sms.model.dto.LiveCallbackDTO();
        dto.setEventType(eventType);
        dto.setStreamId(streamName);
        dto.setSequence("seq-1");
        dto.setErrcode(0);
        dto.setErrmsg("ok");
        return dto;
    }

    /**
     * 回调链路通用桩：跳过签名 + 开启状态驱动 + 按流名命中房间
     */
    private void stubCallback(String streamName, SmsLiveRoom room) {
        when(liveProperties.getCallbackKey()).thenReturn("");
        when(liveProperties.getCallbackDriveStatus()).thenReturn(true);
        doReturn(room).when(liveRoomService).getOne(any(Wrapper.class));
        if (room != null) {
            doReturn(room).when(liveRoomService).getById(room.getId());
        }
    }

    @Test
    @DisplayName("回调-推流：未开播 → 直播中，写入开播时间")
    void callback_streamBegin_shouldStartLive() {
        SmsLiveRoom room = buildRoom(1L, LiveRoomStatusEnum.NOT_STARTED.getValue());
        stubCallback(room.getStreamName(), room);

        assertTrue(liveRoomService.handleTencentCallback(
                buildCallback(LiveCallbackEventEnum.STREAM_BEGIN.getValue(), room.getStreamName())));

        assertEquals(LiveRoomStatusEnum.LIVING.getValue(), room.getStatus());
        assertNotNull(room.getLiveStartTime());
        assertNull(room.getLiveEndTime());
        verify(liveRoomService).updateById(room);
    }

    @Test
    @DisplayName("回调-推流幂等：已在直播中则不改动，开播时间不被重试冲掉")
    void callback_streamBegin_whenAlreadyLiving_shouldKeepOriginalStartTime() {
        SmsLiveRoom room = buildRoom(1L, LiveRoomStatusEnum.LIVING.getValue());
        LocalDateTime originalStart = LocalDateTime.now().minusHours(2);
        room.setLiveStartTime(originalStart);
        stubCallback(room.getStreamName(), room);

        assertTrue(liveRoomService.handleTencentCallback(
                buildCallback(LiveCallbackEventEnum.STREAM_BEGIN.getValue(), room.getStreamName())));

        assertEquals(LiveRoomStatusEnum.LIVING.getValue(), room.getStatus());
        assertEquals(originalStart, room.getLiveStartTime(), "重复推流回调不得重置开播时间");
        verify(liveRoomService, Mockito.never()).updateById(any());
    }

    @Test
    @DisplayName("回调-断流：直播中 → 未开播，写入下播时间")
    void callback_streamEnd_shouldStopLive() {
        SmsLiveRoom room = buildRoom(1L, LiveRoomStatusEnum.LIVING.getValue());
        room.setLiveStartTime(LocalDateTime.now().minusHours(1));
        stubCallback(room.getStreamName(), room);

        assertTrue(liveRoomService.handleTencentCallback(
                buildCallback(LiveCallbackEventEnum.STREAM_END.getValue(), room.getStreamName())));

        assertEquals(LiveRoomStatusEnum.NOT_STARTED.getValue(), room.getStatus());
        assertNotNull(room.getLiveEndTime());
        verify(liveRoomService).updateById(room);
    }

    @Test
    @DisplayName("回调-断流幂等：已是未开播则不改动")
    void callback_streamEnd_whenAlreadyNotStarted_shouldSkip() {
        SmsLiveRoom room = buildRoom(1L, LiveRoomStatusEnum.NOT_STARTED.getValue());
        stubCallback(room.getStreamName(), room);

        assertTrue(liveRoomService.handleTencentCallback(
                buildCallback(LiveCallbackEventEnum.STREAM_END.getValue(), room.getStreamName())));

        assertEquals(LiveRoomStatusEnum.NOT_STARTED.getValue(), room.getStatus());
        assertNull(room.getLiveEndTime());
        verify(liveRoomService, Mockito.never()).updateById(any());
    }

    @Test
    @DisplayName("回调-封禁保护：封禁房间不被推流事件自动解除")
    void callback_bannedRoom_shouldBeIgnored() {
        SmsLiveRoom room = buildRoom(1L, LiveRoomStatusEnum.BANNED.getValue());
        stubCallback(room.getStreamName(), room);

        assertTrue(liveRoomService.handleTencentCallback(
                buildCallback(LiveCallbackEventEnum.STREAM_BEGIN.getValue(), room.getStreamName())));

        assertEquals(LiveRoomStatusEnum.BANNED.getValue(), room.getStatus());
        verify(liveRoomService, Mockito.never()).updateById(any());
    }

    @Test
    @DisplayName("回调-未匹配房间：返回 true（避免腾讯云无意义重试），不改任何状态")
    void callback_unknownStream_shouldReturnTrueWithoutChange() {
        when(liveProperties.getCallbackKey()).thenReturn("");
        doReturn(null).when(liveRoomService).getOne(any(Wrapper.class));

        assertTrue(liveRoomService.handleTencentCallback(
                buildCallback(LiveCallbackEventEnum.STREAM_BEGIN.getValue(), "not-exist-stream")));

        verify(liveRoomService, Mockito.never()).updateById(any());
    }

    @Test
    @DisplayName("回调-缺少流名：返回 true 不抛异常")
    void callback_missingStreamName_shouldReturnTrue() {
        when(liveProperties.getCallbackKey()).thenReturn("");

        assertTrue(liveRoomService.handleTencentCallback(
                buildCallback(LiveCallbackEventEnum.STREAM_BEGIN.getValue(), null)));
    }

    @Test
    @DisplayName("回调-录制事件：本期仅记日志，不驱动状态")
    void callback_recordEvent_shouldNotDriveStatus() {
        when(liveProperties.getCallbackKey()).thenReturn("");

        assertTrue(liveRoomService.handleTencentCallback(
                buildCallback(LiveCallbackEventEnum.RECORD_FILE.getValue(), "st1")));

        verify(liveRoomService, Mockito.never()).updateById(any());
    }

    @Test
    @DisplayName("回调-签名非法：返回 false 且不改状态")
    void callback_invalidSign_shouldReturnFalse() {
        when(liveProperties.getCallbackKey()).thenReturn("prod-key");
        com.deko.mall.sms.model.dto.LiveCallbackDTO dto =
                buildCallback(LiveCallbackEventEnum.STREAM_BEGIN.getValue(), "st1");
        dto.setT(System.currentTimeMillis() / 1000L + 600);
        dto.setSign("tampered-sign");

        assertFalse(liveRoomService.handleTencentCallback(dto));
        verify(liveRoomService, Mockito.never()).updateById(any());
    }

    @Test
    @DisplayName("回调-签名过期：返回 false（防重放）")
    void callback_expiredSign_shouldReturnFalse() {
        String key = "prod-key";
        when(liveProperties.getCallbackKey()).thenReturn(key);
        long t = System.currentTimeMillis() / 1000L - 700;
        com.deko.mall.sms.model.dto.LiveCallbackDTO dto =
                buildCallback(LiveCallbackEventEnum.STREAM_BEGIN.getValue(), "st1");
        dto.setT(t);
        dto.setSign(com.deko.mall.sms.util.LiveCallbackSignUtils.sign(key, t));

        assertFalse(liveRoomService.handleTencentCallback(dto));
        verify(liveRoomService, Mockito.never()).updateById(any());
    }

    @Test
    @DisplayName("回调-签名合法且未过期：正常驱动状态")
    void callback_validSign_shouldDriveStatus() {
        String key = "prod-key";
        SmsLiveRoom room = buildRoom(1L, LiveRoomStatusEnum.NOT_STARTED.getValue());
        when(liveProperties.getCallbackKey()).thenReturn(key);
        when(liveProperties.getCallbackDriveStatus()).thenReturn(true);
        doReturn(room).when(liveRoomService).getOne(any(Wrapper.class));
        doReturn(room).when(liveRoomService).getById(1L);
        long t = System.currentTimeMillis() / 1000L + 600;
        com.deko.mall.sms.model.dto.LiveCallbackDTO dto =
                buildCallback(LiveCallbackEventEnum.STREAM_BEGIN.getValue(), room.getStreamName());
        dto.setT(t);
        dto.setSign(com.deko.mall.sms.util.LiveCallbackSignUtils.sign(key, t));

        assertTrue(liveRoomService.handleTencentCallback(dto));
        assertEquals(LiveRoomStatusEnum.LIVING.getValue(), room.getStatus());
    }

    @Test
    @DisplayName("回调-状态驱动开关关闭：只记录不改状态")
    void callback_driveStatusOff_shouldNotChangeStatus() {
        SmsLiveRoom room = buildRoom(1L, LiveRoomStatusEnum.NOT_STARTED.getValue());
        when(liveProperties.getCallbackKey()).thenReturn("");
        when(liveProperties.getCallbackDriveStatus()).thenReturn(false);
        doReturn(room).when(liveRoomService).getOne(any(Wrapper.class));

        assertTrue(liveRoomService.handleTencentCallback(
                buildCallback(LiveCallbackEventEnum.STREAM_BEGIN.getValue(), room.getStreamName())));

        assertEquals(LiveRoomStatusEnum.NOT_STARTED.getValue(), room.getStatus());
        verify(liveRoomService, Mockito.never()).updateById(any());
    }
}
