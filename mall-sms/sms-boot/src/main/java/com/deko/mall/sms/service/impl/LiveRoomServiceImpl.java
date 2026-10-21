package com.deko.mall.sms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.common.base.IBaseEnum;
import com.deko.mall.common.result.Result;
import com.deko.mall.common.web.exception.BizException;
import com.deko.mall.pms.api.SpuFeignClient;
import com.deko.mall.pms.model.dto.PmsSpuDTO;
import com.deko.mall.pms.model.dto.SpuDynamicDataDTO;
import com.deko.mall.sms.config.LiveProperties;
import com.deko.mall.sms.config.SmsResultCode;
import com.deko.mall.sms.constant.LiveConstants;
import com.deko.mall.sms.converter.LiveConverter;
import com.deko.mall.sms.enums.LiveCallbackEventEnum;
import com.deko.mall.sms.enums.LiveRoomStatusEnum;
import com.deko.mall.sms.mapper.LiveRoomMapper;
import com.deko.mall.sms.model.dto.LiveCallbackDTO;
import com.deko.mall.sms.model.entity.SmsLiveRoom;
import com.deko.mall.sms.model.entity.SmsLiveRoomGoods;
import com.deko.mall.sms.model.form.LiveRoomForm;
import com.deko.mall.sms.model.form.LiveRoomGoodsForm;
import com.deko.mall.sms.model.query.LiveRoomPageQuery;
import com.deko.mall.sms.model.vo.*;
import com.deko.mall.sms.service.AdvertService;
import com.deko.mall.sms.service.LiveRoomGoodsService;
import com.deko.mall.sms.service.LiveRoomService;
import com.deko.mall.sms.util.LiveCallbackSignUtils;
import com.deko.mall.sms.util.LiveStreamUrlGenerator;
import com.deko.mall.sms.websocket.LiveSessionManager;
import com.deko.mall.sms.websocket.LiveWebSocketMessageBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 直播间服务实现
 * <p>
 * 出参 VO 统一 snake_case（@JsonNaming），供前端直接消费。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LiveRoomServiceImpl extends ServiceImpl<LiveRoomMapper, SmsLiveRoom> implements LiveRoomService {

    private final LiveConverter liveConverter;
    private final LiveRoomGoodsService liveRoomGoodsService;
    private final LiveStreamUrlGenerator streamUrlGenerator;
    private final LiveProperties liveProperties;
    private final RedisTemplate<String, Object> redisTemplate;
    private final LiveSessionManager sessionManager;
    private final SpuFeignClient spuFeignClient;
    private final LiveWebSocketMessageBuilder messageBuilder;
    /** 复用广告位能力为直播首页提供轮播图（字段需映射到 slider 组件契约，见 LiveSliderVO） */
    private final AdvertService advertService;

    // APP 端

    @Override
    public LiveIndexInitVO getInitData() {
        List<LiveStatusTabVO> tabs = Arrays.asList(
                new LiveStatusTabVO("", "全部"),
                new LiveStatusTabVO("1", "直播中"),
                new LiveStatusTabVO("2", "离开"),
                new LiveStatusTabVO("0", "未开播")
        );
        LiveBaseVO base = new LiveBaseVO();
        base.setApplicationName(liveProperties.getApplicationName());
        base.setSeoTitle(liveProperties.getApplicationName());

        LiveIndexInitVO vo = new LiveIndexInitVO();
        vo.setLiveStatusTabList(tabs);
        vo.setSliderList(buildSliderList());
        vo.setSearchOrderByList(defaultOrderByList());
        vo.setBase(base);
        return vo;
    }

    /**
     * 搜索排序依据（前端 search_order_by_list 契约：{name,value,checked}）
     * <p>
     * value 会作为接口 by 参数回传，由 {@link #applyOrderBy} 消费。
     */
    private List<LiveOrderByVO> defaultOrderByList() {
        return Arrays.asList(
                new LiveOrderByVO("默认", LiveConstants.LIVE_ORDER_BY_DEFAULT, true),
                new LiveOrderByVO("人气", LiveConstants.LIVE_ORDER_BY_HOT, false),
                new LiveOrderByVO("最新", LiveConstants.LIVE_ORDER_BY_NEW, false)
        );
    }

    /**
     * 直播首页轮播图：复用广告位能力，映射为 slider 组件消费的字段名
     * <p>
     * 注意不能直接返回 BannerVO —— 组件读的是 images_url/event_value/event_type，
     * 字段不匹配会导致图片取不到地址。广告位未配置时降级为空列表（前端 v-if 自动隐藏）。
     */
    private List<LiveSliderVO> buildSliderList() {
        try {
            List<BannerVO> banners = advertService.getBannerList();
            if (CollUtil.isEmpty(banners)) {
                return List.of();
            }
            return banners.stream()
                    .filter(b -> b != null && StrUtil.isNotBlank(b.getImageUrl()))
                    .map(b -> new LiveSliderVO(b.getImageUrl(), b.getRedirectUrl(), 1, b.getRedirectUrl()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("直播首页轮播图加载失败，降级为空列表", e);
            return List.of();
        }
    }

    /**
     * 应用排序依据（前端 by 参数：default-默认 / hot-人气 / new-最新）
     * <p>
     * 人气取自 DB 的 like_count 快照（下播时回写），Redis 实时值不参与排序。
     */
    private void applyOrderBy(LambdaQueryWrapper<SmsLiveRoom> wrapper, String by) {
        if (LiveConstants.LIVE_ORDER_BY_HOT.equals(by)) {
            wrapper.orderByDesc(SmsLiveRoom::getLikeCount).orderByAsc(SmsLiveRoom::getSort);
        } else if (LiveConstants.LIVE_ORDER_BY_NEW.equals(by)) {
            wrapper.orderByDesc(SmsLiveRoom::getCreateTime);
        } else {
            wrapper.orderByAsc(SmsLiveRoom::getSort);
        }
    }

    @Override
    public LiveRoomListDataVO getRoomList(String status, String keyword, String by, Integer page, Integer pageSize) {
        LambdaQueryWrapper<SmsLiveRoom> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(status)) {
            wrapper.eq(SmsLiveRoom::getStatus, Integer.parseInt(status));
        }
        // 封禁直播间不对外展示
        wrapper.ne(SmsLiveRoom::getStatus, LiveRoomStatusEnum.BANNED.getValue());
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(SmsLiveRoom::getTitle, keyword)
                    .or().like(SmsLiveRoom::getAnchorName, keyword)
                    .or().like(SmsLiveRoom::getRoomNo, keyword));
        }
        applyOrderBy(wrapper, by);
        int pageNum = page == null || page < 1 ? 1 : page;
        int size = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 100);
        Page<SmsLiveRoom> result = this.page(new Page<>(pageNum, size), wrapper);

        List<LiveRoomListVO> list = liveConverter.entity2ListVo(result.getRecords());
        list.forEach(v -> {
            v.setStatusName(IBaseEnum.getLabelByValue(v.getStatus(), LiveRoomStatusEnum.class));
            v.setUrl(LiveConstants.LIVE_DETAIL_PAGE_PREFIX + v.getId());
            // 在线人数实时取 Redis（WS 会话数），避免显示 DB 下播快照
            v.setOnlineCount((int) getOnlineCount(v.getId()));
        });
        LiveRoomListDataVO data = new LiveRoomListDataVO();
        data.setDataList(list);
        data.setData(list);
        data.setPageTotal(result.getPages());
        data.setTotal(result.getTotal());
        return data;
    }

    @Override
    public List<LiveRoomListVO> getDiyRoomList(String keywords, String status, Integer number, Integer isRecommended) {
        LambdaQueryWrapper<SmsLiveRoom> wrapper = new LambdaQueryWrapper<>();
        // 封禁不展示
        wrapper.ne(SmsLiveRoom::getStatus, LiveRoomStatusEnum.BANNED.getValue());
        // 状态支持多选：编辑器「状态筛选」可勾多项，前端以逗号串下发（如 "0,1"）；
        // 兼容单值 "1"。历史写法 Integer 参数遇到 "0,1" 会绑定失败直接 400。
        List<Integer> statusList = parseStatusList(status);
        if (!statusList.isEmpty()) {
            wrapper.in(SmsLiveRoom::getStatus, statusList);
        }
        // 装修组件的「推荐」筛选（null 表示不筛选）
        if (isRecommended != null) {
            wrapper.eq(SmsLiveRoom::getIsRecommended, isRecommended);
        }
        if (StrUtil.isNotBlank(keywords)) {
            wrapper.and(w -> w.like(SmsLiveRoom::getTitle, keywords)
                    .or().like(SmsLiveRoom::getAnchorName, keywords)
                    .or().like(SmsLiveRoom::getRoomNo, keywords));
        }
        wrapper.orderByAsc(SmsLiveRoom::getSort);
        // 装修组件期望数组结构，按数量截断
        wrapper.last("LIMIT " + Math.min(number == null ? 10 : number, 50));
        List<SmsLiveRoom> rooms = this.list(wrapper);

        List<LiveRoomListVO> list = liveConverter.entity2ListVo(rooms);
        list.forEach(v -> {
            v.setStatusName(IBaseEnum.getLabelByValue(v.getStatus(), LiveRoomStatusEnum.class));
            v.setUrl(LiveConstants.LIVE_DETAIL_PAGE_PREFIX + v.getId());
            // 在线人数实时取 Redis（WS 会话数），避免显示 DB 下播快照
            v.setOnlineCount((int) getOnlineCount(v.getId()));
        });
        return list;
    }

    /**
     * 解析状态筛选（兼容单选 "1" 与多选 "0,1"）
     * <p>
     * 忽略空白与非法值，并剔除封禁态（封禁直播间本就不对外展示）。
     *
     * @param status 逗号分隔的状态串，可为空
     * @return 状态列表，空表示不筛选
     */
    private List<Integer> parseStatusList(String status) {
        if (StrUtil.isBlank(status)) {
            return List.of();
        }
        return Arrays.stream(status.split(","))
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .map(item -> {
                    try {
                        return Integer.valueOf(item);
                    } catch (NumberFormatException e) {
                        log.warn("直播装修取数忽略非法状态值: {}", item);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(item -> !Objects.equals(item, LiveRoomStatusEnum.BANNED.getValue()))
                .distinct()
                .toList();
    }

    @Override
    public LiveSearchRecordVO getSearchRecordData() {
        LiveSearchRecordVO vo = new LiveSearchRecordVO();
        // 一期：热门搜索为空，前端展示本地搜索历史
        vo.setSearchHistoryData(List.of());
        return vo;
    }

    @Override
    public LiveRoomPageVO getAdminDetail(Long id) {
        SmsLiveRoom room = getRoomEntity(id);
        LiveRoomPageVO vo = liveConverter.entity2PageVo(room);
        vo.setStatusName(IBaseEnum.getLabelByValue(vo.getStatus(), LiveRoomStatusEnum.class));
        return vo;
    }

    @Override
    public LiveRoomDetailVO getRoomDetail(Long id) {
        SmsLiveRoom room = getRoomEntity(id);

        LiveRoomInfoVO info = liveConverter.entity2InfoVo(room);
        info.setOnlineCount((int) getOnlineCount(id));
        info.setLikeCount((int) getLikeCount(id));

        LiveRoomConfigVO config = new LiveRoomConfigVO();
        config.setIsLiveChat(room.getIsLiveChat());
        config.setIsLiveGoodsBuy(room.getIsLiveGoodsBuy());
        config.setIsLiveLike(room.getIsLiveLike());
        config.setLiveReconnectNumber(room.getLiveReconnectNumber());
        config.setLiveReconnectIntervalTime(room.getLiveReconnectIntervalTime());
        config.setLiveGoodsExplainAutoCloseTime(room.getLiveGoodsExplainAutoCloseTime());
        // 前端 setInterval 周期，不下发会让间隔变成 NaN(≈0) 疯狂空转
        config.setLiveInfoTimingIntervalTime(LiveConstants.LIVE_INFO_POLL_INTERVAL_SECONDS);
        config.setLiveTips(room.getLiveTips());
        LiveSocketConnectVO socket = new LiveSocketConnectVO();
        socket.setHost(liveProperties.getWsHost());
        socket.setPort(liveProperties.getWsPort());
        socket.setIsWss(liveProperties.getWsIsWss());
        // 经统一网关时为网关前缀（如 /mall-sms/），本地直连为 /
        socket.setPath(liveProperties.getWsPath());
        config.setSocketConnect(socket);

        LiveRoomDetailVO vo = new LiveRoomDetailVO();
        // 顶层 id 必须下发：前端 live_data.id 用于 WS init 的 live_id、商品 propLiveId、分享参数
        vo.setId(room.getId());
        vo.setLiveInfo(info);
        vo.setConfig(config);
        vo.setTitle(room.getTitle());
        vo.setCover(room.getCover());
        vo.setAnchorName(room.getAnchorName());
        vo.setAnchorAvatar(room.getAnchorAvatar());
        vo.setOnlineCount((int) getOnlineCount(id));
        return vo;
    }

    @Override
    public List<LiveGoodsVO> getRoomGoods(Long roomId) {
        getRoomEntity(roomId);
        List<SmsLiveRoomGoods> goods = liveRoomGoodsService.list(
                new LambdaQueryWrapper<SmsLiveRoomGoods>()
                        .eq(SmsLiveRoomGoods::getRoomId, roomId)
                        .orderByAsc(SmsLiveRoomGoods::getSort));
        List<LiveGoodsVO> vos = liveConverter.goods2Vo(goods);
        vos.forEach(v -> v.setGoodsUrl(LiveConstants.GOODS_DETAIL_PAGE_PREFIX + v.getSpuId()));
        return vos;
    }

    // 管理端

    @Override
    public Page<LiveRoomPageVO> getRoomPage(LiveRoomPageQuery query) {
        LambdaQueryWrapper<SmsLiveRoom> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(query.getKeywords())) {
            wrapper.and(w -> w.like(SmsLiveRoom::getTitle, query.getKeywords())
                    .or().like(SmsLiveRoom::getAnchorName, query.getKeywords())
                    .or().like(SmsLiveRoom::getRoomNo, query.getKeywords()));
        }
        if (query.getStatus() != null) {
            wrapper.eq(SmsLiveRoom::getStatus, query.getStatus());
        }
        wrapper.orderByAsc(SmsLiveRoom::getSort);
        Page<SmsLiveRoom> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        Page<LiveRoomPageVO> voPage = liveConverter.entity2PageVo(page);
        voPage.getRecords().forEach(v ->
                v.setStatusName(IBaseEnum.getLabelByValue(v.getStatus(), LiveRoomStatusEnum.class)));
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRoom(LiveRoomForm form) {
        SmsLiveRoom room = new SmsLiveRoom();
        BeanUtil.copyProperties(form, room);
        // 流名缺省生成
        if (StrUtil.isBlank(room.getStreamName())) {
            room.setStreamName(generateStreamName());
        }
        checkStreamNameUnique(room.getStreamName(), null);
        // 直播间编号：snowflake 后 6 位（前 6 位为时间戳高位，同一天内几乎不变，会撞 uk_room_no）
        String snowflakeId = IdUtil.getSnowflakeNextIdStr();
        room.setRoomNo("L" + DateUtil.format(new Date(), "yyyyMMdd")
                + snowflakeId.substring(Math.max(0, snowflakeId.length() - 6)));
        room.setStatus(LiveRoomStatusEnum.NOT_STARTED.getValue());
        room.setOnlineCount(0);
        room.setLikeCount(0);
        // 默认功能开关
        if (room.getIsLiveChat() == null) {
            room.setIsLiveChat(1);
        }
        if (room.getIsLiveGoodsBuy() == null) {
            room.setIsLiveGoodsBuy(1);
        }
        if (room.getIsLiveLike() == null) {
            room.setIsLiveLike(1);
        }
        if (room.getLiveReconnectNumber() == null) {
            room.setLiveReconnectNumber(3);
        }
        if (room.getLiveReconnectIntervalTime() == null) {
            room.setLiveReconnectIntervalTime(3);
        }
        if (room.getLiveGoodsExplainAutoCloseTime() == null) {
            room.setLiveGoodsExplainAutoCloseTime(10);
        }
        if (room.getIsRecommended() == null) {
            room.setIsRecommended(0);
        }
        // 生成推拉流地址
        room.setPushUrl(streamUrlGenerator.generatePushUrl(room.getStreamName()));
        room.setPullUrl(streamUrlGenerator.generatePullUrl(room.getStreamName()));
        this.save(room);
        return room.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRoom(LiveRoomForm form) {
        SmsLiveRoom room = getRoomEntity(form.getId());
        // 忽略 null 字段，避免编辑时未回填的字段把数据库已有值清空
        BeanUtil.copyProperties(form, room,
                cn.hutool.core.bean.copier.CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setIgnoreProperties("id", "roomNo", "streamName", "pushUrl", "pullUrl", "status",
                                "onlineCount", "likeCount", "liveStartTime", "liveEndTime"));
        this.updateById(room);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRooms(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        this.removeByIds(ids);
        liveRoomGoodsService.remove(new LambdaQueryWrapper<SmsLiveRoomGoods>()
                .in(SmsLiveRoomGoods::getRoomId, ids));
        // 清理 Redis 计数
        ids.forEach(id -> {
            redisTemplate.delete(LiveConstants.LIVE_ROOM_ONLINE_PREFIX + id);
            redisTemplate.delete(LiveConstants.LIVE_ROOM_LIKE_PREFIX + id);
            redisTemplate.delete(LiveConstants.LIVE_ROOM_STATUS_PREFIX + id);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindGoods(LiveRoomGoodsForm form) {
        getRoomEntity(form.getRoomId());
        if (form.getSpuId() == null) {
            throw new BizException(SmsResultCode.PARAM_INVALID);
        }
        // 幂等：已绑定直接返回
        long count = liveRoomGoodsService.count(new LambdaQueryWrapper<SmsLiveRoomGoods>()
                .eq(SmsLiveRoomGoods::getRoomId, form.getRoomId())
                .eq(SmsLiveRoomGoods::getSpuId, form.getSpuId()));
        if (count > 0) {
            throw new BizException(SmsResultCode.LIVE_GOODS_EXISTS);
        }
        // 拉取商品快照
        PmsSpuDTO spu = fetchSpu(form.getSpuId());
        SmsLiveRoomGoods goods = new SmsLiveRoomGoods();
        goods.setRoomId(form.getRoomId());
        goods.setSpuId(form.getSpuId());
        goods.setGoodsTitle(spu.getName());
        goods.setGoodsImage(spu.getPicUrl());
        goods.setGoodsPrice(spu.getPrice());
        goods.setInventory(fetchSpuStock(form.getSpuId()));
        goods.setIsExplain(0);
        goods.setSort(form.getSort() == null ? 0 : form.getSort());
        liveRoomGoodsService.save(goods);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindGoods(Long goodsId) {
        SmsLiveRoomGoods goods = liveRoomGoodsService.getById(goodsId);
        if (goods == null) {
            throw new BizException(SmsResultCode.LIVE_GOODS_NOT_FOUND);
        }
        liveRoomGoodsService.removeById(goodsId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setExplainGoods(Long goodsId) {
        SmsLiveRoomGoods goods = liveRoomGoodsService.getById(goodsId);
        if (goods == null) {
            throw new BizException(SmsResultCode.LIVE_GOODS_NOT_FOUND);
        }
        // 同房间其他商品取消讲解
        liveRoomGoodsService.update(new LambdaUpdateWrapper<SmsLiveRoomGoods>()
                .eq(SmsLiveRoomGoods::getRoomId, goods.getRoomId())
                .ne(SmsLiveRoomGoods::getId, goodsId)
                .set(SmsLiveRoomGoods::getIsExplain, 0));
        liveRoomGoodsService.update(new LambdaUpdateWrapper<SmsLiveRoomGoods>()
                .eq(SmsLiveRoomGoods::getId, goodsId)
                .set(SmsLiveRoomGoods::getIsExplain, 1));
        // 推送讲解商品变更（前端讲解弹窗）
        pushLiveInfo(goods.getRoomId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, Integer status) {
        SmsLiveRoom room = getRoomEntity(id);
        LiveRoomStatusEnum target = IBaseEnum.getEnumByValue(status, LiveRoomStatusEnum.class);
        if (target == null) {
            throw new BizException(SmsResultCode.LIVE_ROOM_STATUS_INVALID);
        }
        int current = room.getStatus() == null ? 0 : room.getStatus();
        // 封禁直播间不可直接开播（需先解封=置为未开播）
        if (LiveRoomStatusEnum.BANNED.getValue().equals(target.getValue()) && current == LiveRoomStatusEnum.BANNED.getValue()) {
            throw new BizException(SmsResultCode.LIVE_ROOM_STATUS_INVALID);
        }
        LocalDateTime now = LocalDateTime.now();
        if (LiveRoomStatusEnum.LIVING.getValue().equals(target.getValue())) {
            // 开播：重置开播时间
            room.setLiveStartTime(now);
            room.setLiveEndTime(null);
        } else if (LiveRoomStatusEnum.NOT_STARTED.getValue().equals(target.getValue())) {
            // 下播：回写计数、记录下播时间
            persistCountsToDb(room);
            room.setLiveEndTime(now);
            redisTemplate.delete(LiveConstants.LIVE_ROOM_ONLINE_PREFIX + id);
            redisTemplate.delete(LiveConstants.LIVE_ROOM_LIKE_PREFIX + id);
            redisTemplate.delete(LiveConstants.LIVE_ROOM_STATUS_PREFIX + id);
        }
        room.setStatus(target.getValue());
        this.updateById(room);
        // 心跳 + 广播状态变更
        if (!LiveRoomStatusEnum.NOT_STARTED.getValue().equals(target.getValue())
                && !LiveRoomStatusEnum.BANNED.getValue().equals(target.getValue())) {
            heartbeat(id, target.getValue());
        }
        pushLiveInfo(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LiveRoomPageVO refreshStreamUrl(Long id) {
        SmsLiveRoom room = getRoomEntity(id);
        room.setPushUrl(streamUrlGenerator.generatePushUrl(room.getStreamName()));
        room.setPullUrl(streamUrlGenerator.generatePullUrl(room.getStreamName()));
        this.updateById(room);
        return liveConverter.entity2PageVo(room);
    }

    // 状态/计数

    @Override
    public boolean isChatEnabled(Long roomId) {
        SmsLiveRoom room = this.getById(roomId);
        return room != null && Integer.valueOf(1).equals(room.getIsLiveChat());
    }

    @Override
    public void heartbeat(Long roomId, Integer status) {
        redisTemplate.opsForValue().set(LiveConstants.LIVE_ROOM_STATUS_PREFIX + roomId,
                status, LiveConstants.LIVE_ROOM_STATUS_TTL_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public long getOnlineCount(Long roomId) {
        Object count = redisTemplate.opsForValue().get(LiveConstants.LIVE_ROOM_ONLINE_PREFIX + roomId);
        if (count != null) {
            return Long.parseLong(String.valueOf(count));
        }
        SmsLiveRoom room = this.getById(roomId);
        return room == null || room.getOnlineCount() == null ? 0L : room.getOnlineCount();
    }

    @Override
    public long getLikeCount(Long roomId) {
        Object count = redisTemplate.opsForValue().get(LiveConstants.LIVE_ROOM_LIKE_PREFIX + roomId);
        if (count != null) {
            return Long.parseLong(String.valueOf(count));
        }
        SmsLiveRoom room = this.getById(roomId);
        return room == null || room.getLikeCount() == null ? 0L : room.getLikeCount();
    }

    @Override
    public void addLike(Long roomId, int count) {
        redisTemplate.opsForValue().increment(LiveConstants.LIVE_ROOM_LIKE_PREFIX + roomId, count);
        redisTemplate.expire(LiveConstants.LIVE_ROOM_LIKE_PREFIX + roomId,
                LiveConstants.LIVE_ROOM_COUNT_TTL_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public LiveRoomDetailVO buildLiveInfoPush(Long roomId) {
        SmsLiveRoom room = this.getById(roomId);
        if (room == null) {
            return null;
        }
        // live_info（含实时计数）
        LiveRoomInfoVO info = liveConverter.entity2InfoVo(room);
        info.setOnlineCount((int) getOnlineCount(roomId));
        info.setLikeCount((int) getLikeCount(roomId));
        // 快照（讲解商品 + 在线用户头像）
        LiveRoomDetailVO vo = new LiveRoomDetailVO();
        vo.setId(room.getId());
        vo.setLiveInfo(info);
        vo.setExplainGoods(getExplainGoods(roomId));
        vo.setOnlineUser(sessionManager.getOnlineUserAvatars(roomId));
        return vo;
    }

    @Override
    public void autoDetectOfflineRooms() {
        List<SmsLiveRoom> rooms = this.list(new LambdaQueryWrapper<SmsLiveRoom>()
                .in(SmsLiveRoom::getStatus, LiveRoomStatusEnum.LIVING.getValue(), LiveRoomStatusEnum.AWAY.getValue()));
        for (SmsLiveRoom room : rooms) {
            boolean alive = probeStreamAlive(room);
            if (alive) {
                // 探测成功：刷新心跳 TTL，清空失败计数
                heartbeat(room.getId(), room.getStatus());
                redisTemplate.delete(LiveConstants.LIVE_ROOM_PROBE_FAIL_PREFIX + room.getId());
            } else {
                // 探测失败：连续失败达到阈值才判下播，避免瞬时抖动误判
                Long failCount = redisTemplate.opsForValue().increment(LiveConstants.LIVE_ROOM_PROBE_FAIL_PREFIX + room.getId());
                redisTemplate.expire(LiveConstants.LIVE_ROOM_PROBE_FAIL_PREFIX + room.getId(),
                        LiveConstants.LIVE_ROOM_COUNT_TTL_SECONDS, TimeUnit.SECONDS);
                if (failCount != null && failCount >= LiveConstants.LIVE_ROOM_PROBE_FAIL_THRESHOLD) {
                    log.warn("直播间[{}]拉流探测连续失败{}次，自动下播", room.getId(), failCount);
                    changeStatus(room.getId(), LiveRoomStatusEnum.NOT_STARTED.getValue());
                    redisTemplate.delete(LiveConstants.LIVE_ROOM_PROBE_FAIL_PREFIX + room.getId());
                }
            }
        }
    }

    // 内部方法

    /**
     * 获取房间实体（不存在抛业务异常）
     */
    private SmsLiveRoom getRoomEntity(Long id) {
        SmsLiveRoom room = this.getById(id);
        if (room == null) {
            throw new BizException(SmsResultCode.LIVE_ROOM_NOT_FOUND);
        }
        return room;
    }

    /**
     * 校验流名唯一
     */
    private void checkStreamNameUnique(String streamName, Long excludeId) {
        LambdaQueryWrapper<SmsLiveRoom> wrapper = new LambdaQueryWrapper<SmsLiveRoom>()
                .eq(SmsLiveRoom::getStreamName, streamName);
        if (excludeId != null) {
            wrapper.ne(SmsLiveRoom::getId, excludeId);
        }
        if (this.count(wrapper) > 0) {
            throw new BizException(SmsResultCode.LIVE_STREAM_NAME_EXISTS);
        }
    }

    /**
     * 生成直播流名
     */
    private String generateStreamName() {
        return "st" + IdUtil.getSnowflakeNextIdStr();
    }

    /**
     * 拉取商品快照
     */
    private PmsSpuDTO fetchSpu(Long spuId) {
        Result<List<PmsSpuDTO>> result = spuFeignClient.getSpuListForApp(List.of(spuId));
        if (result == null || !"00000".equals(result.getCode()) || CollUtil.isEmpty(result.getData())) {
            throw new BizException(SmsResultCode.PARAM_INVALID, "商品不存在");
        }
        return result.getData().get(0);
    }

    /**
     * 拉取商品库存快照
     */
    private int fetchSpuStock(Long spuId) {
        try {
            Result<Map<String, SpuDynamicDataDTO>> result = spuFeignClient.getSpuDynamicData(List.of(spuId));
            if (result != null && "00000".equals(result.getCode()) && CollUtil.isNotEmpty(result.getData())) {
                SpuDynamicDataDTO data = result.getData().get("s" + spuId);
                return data != null && data.getTotalStock() != null ? data.getTotalStock() : 0;
            }
        } catch (Exception e) {
            log.warn("拉取商品[{}]库存失败，使用0", spuId, e);
        }
        return 0;
    }

    /**
     * 当前讲解商品（WS live-info explain_goods）
     */
    private LiveGoodsVO getExplainGoods(Long roomId) {
        SmsLiveRoomGoods goods = liveRoomGoodsService.getOne(new LambdaQueryWrapper<SmsLiveRoomGoods>()
                .eq(SmsLiveRoomGoods::getRoomId, roomId)
                .eq(SmsLiveRoomGoods::getIsExplain, 1)
                .last("LIMIT 1"));
        if (goods == null) {
            return null;
        }
        LiveGoodsVO vo = liveConverter.goods2Vo(goods);
        vo.setGoodsUrl(LiveConstants.GOODS_DETAIL_PAGE_PREFIX + goods.getSpuId());
        return vo;
    }

    /**
     * 下播回写计数到 DB
     */
    private void persistCountsToDb(SmsLiveRoom room) {
        long online = getOnlineCount(room.getId());
        long like = getLikeCount(room.getId());
        this.update(new LambdaUpdateWrapper<SmsLiveRoom>()
                .eq(SmsLiveRoom::getId, room.getId())
                .set(SmsLiveRoom::getOnlineCount, online)
                .set(SmsLiveRoom::getLikeCount, like));
    }

    /**
     * 拉流探测（HEAD 请求 m3u8）
     */
    private boolean probeStreamAlive(SmsLiveRoom room) {
        if (StrUtil.isBlank(room.getPullUrl())) {
            return false;
        }
        return liveStreamProbe(room.getPullUrl() + ".m3u8");
    }

    private boolean liveStreamProbe(String url) {
        try {
            cn.hutool.http.HttpRequest request = cn.hutool.http.HttpRequest.head(url).timeout(3000);
            cn.hutool.http.HttpResponse response = request.execute();
            int status = response.getStatus();
            response.close();
            return status >= 200 && status < 400;
        } catch (Exception e) {
            log.debug("拉流探测失败: {}", url);
            return false;
        }
    }

    /**
     * 向房间广播 live-info（讲解商品/状态/计数变更）
     */
    private void pushLiveInfo(Long roomId) {
        LiveRoomDetailVO snapshot = buildLiveInfoPush(roomId);
        if (snapshot == null) {
            return;
        }
        try {
            sessionManager.broadcast(roomId, messageBuilder.buildLiveInfo(snapshot));
        } catch (Exception e) {
            log.warn("live-info 广播失败, roomId={}", roomId, e);
        }
    }

    // 腾讯云直播回调

    @Override
    public boolean handleTencentCallback(LiveCallbackDTO dto) {
        // ① 签名 + 时效校验。密钥留空则跳过，仅为便于本地联调（生产必须配置）
        String callbackKey = liveProperties.getCallbackKey();
        if (StrUtil.isBlank(callbackKey)) {
            log.warn("腾讯云直播回调未配置 live.callback-key，已跳过签名校验（仅限本地联调）");
        } else {
            if (!LiveCallbackSignUtils.verify(callbackKey, dto.getT(), dto.getSign())) {
                log.warn("腾讯云直播回调签名校验失败 t={} sign={}", dto.getT(), dto.getSign());
                return false;
            }
            if (LiveCallbackSignUtils.isExpired(dto.getT())) {
                log.warn("腾讯云直播回调已过期（防重放）t={}", dto.getT());
                return false;
            }
        }

        // ② 事件识别。stream_id 与 channel_id 同值，取其一即可
        LiveCallbackEventEnum event = IBaseEnum.getEnumByValue(dto.getEventType(), LiveCallbackEventEnum.class);
        String streamName = StrUtil.blankToDefault(dto.getStreamId(), dto.getChannelId());
        log.info("腾讯云直播回调 event={}({}) stream={} sequence={} errcode={} errmsg={} pushDuration={}",
                event == null ? "未知" : event.getLabel(), dto.getEventType(), streamName,
                dto.getSequence(), dto.getErrcode(), dto.getErrmsg(), dto.getPushDuration());
        if (event == null) {
            return true;
        }
        // 录制(100)/截图(200)：回放与截图属二期能力，本期只记日志，避免误判为失败而触发重试
        if (event == LiveCallbackEventEnum.RECORD_FILE || event == LiveCallbackEventEnum.SCREENSHOT_FILE) {
            return true;
        }
        if (StrUtil.isBlank(streamName)) {
            log.warn("腾讯云直播回调缺少 stream_id/channel_id，已忽略");
            return true;
        }

        // ③ 定位直播间
        SmsLiveRoom room = getByStreamName(streamName);
        if (room == null) {
            log.warn("腾讯云直播回调未匹配到直播间 stream={}", streamName);
            return true;
        }
        if (!Boolean.TRUE.equals(liveProperties.getCallbackDriveStatus())) {
            log.info("live.callback-drive-status=false，回调仅记录不驱动状态 roomId={}", room.getId());
            return true;
        }

        // ④ 驱动状态
        driveStatusByCallbackEvent(room, event);
        return true;
    }

    /**
     * 按回调事件驱动直播间状态
     * <p>
     * 幂等判断刻意放在此处而非 {@link #changeStatus} 内：changeStatus 在 1→1 时会把
     * liveStartTime 重置为当前时间，而腾讯云回调会重试，不保护就会冲掉真实开播时间；
     * 放在这里同时也不改变后台手动流转的既有语义，把影响面收在回调链路内。
     */
    private void driveStatusByCallbackEvent(SmsLiveRoom room, LiveCallbackEventEnum event) {
        int current = room.getStatus() == null
                ? LiveRoomStatusEnum.NOT_STARTED.getValue() : room.getStatus();
        // 封禁是运营动作，不应被推流事件自动解除
        if (current == LiveRoomStatusEnum.BANNED.getValue()) {
            log.info("直播间[{}]已封禁，忽略回调事件 {}", room.getId(), event.getLabel());
            return;
        }
        if (event == LiveCallbackEventEnum.STREAM_BEGIN) {
            // 幂等：已在直播中则不动
            if (current == LiveRoomStatusEnum.LIVING.getValue()) {
                return;
            }
            changeStatus(room.getId(), LiveRoomStatusEnum.LIVING.getValue());
        } else if (event == LiveCallbackEventEnum.STREAM_END) {
            // 幂等：已是未开播则不动
            if (current == LiveRoomStatusEnum.NOT_STARTED.getValue()) {
                return;
            }
            changeStatus(room.getId(), LiveRoomStatusEnum.NOT_STARTED.getValue());
        }
    }

    /**
     * 按直播流名查直播间（腾讯云 stream_id 即本系统的 stream_name）
     */
    private SmsLiveRoom getByStreamName(String streamName) {
        return this.getOne(new LambdaQueryWrapper<SmsLiveRoom>()
                .eq(SmsLiveRoom::getStreamName, streamName)
                .last("limit 1"));
    }
}
