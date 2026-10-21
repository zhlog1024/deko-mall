package com.deko.mall.sms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.sms.model.dto.LiveCallbackDTO;
import com.deko.mall.sms.model.entity.SmsLiveRoom;
import com.deko.mall.sms.model.form.LiveRoomForm;
import com.deko.mall.sms.model.form.LiveRoomGoodsForm;
import com.deko.mall.sms.model.query.LiveRoomPageQuery;
import com.deko.mall.sms.model.vo.LiveGoodsVO;
import com.deko.mall.sms.model.vo.LiveIndexInitVO;
import com.deko.mall.sms.model.vo.LiveRoomDetailVO;
import com.deko.mall.sms.model.vo.LiveRoomListDataVO;
import com.deko.mall.sms.model.vo.LiveRoomListVO;
import com.deko.mall.sms.model.vo.LiveRoomPageVO;
import com.deko.mall.sms.model.vo.LiveSearchRecordVO;

import java.util.List;

/**
 * 直播间服务
 */
public interface LiveRoomService extends IService<SmsLiveRoom> {

    // APP 端

    /**
     * 直播首页/搜索初始化数据（indexinit / searchinit）
     */
    LiveIndexInitVO getInitData();

    /**
     * 直播间列表（indexdatalist / searchdatalist）
     *
     * @param status   状态(空-全部;0-未开播;1-直播中;2-离开)
     * @param keyword  关键词(标题/主播)
     * @param by       排序依据（default-默认;hot-人气;new-最新；空等同 default）
     * @param page     页码
     * @param pageSize 每页条数
     */
    LiveRoomListDataVO getRoomList(String status, String keyword, String by, Integer page, Integer pageSize);

    /**
     * 直播搜索记录页数据（searchrecord：热门搜索，一期为空）
     */
    LiveSearchRecordVO getSearchRecordData();

    /**
     * 装修首页直播列表（diy 组件 autolivelist 对应；返回数组，非分页）
     *
     * @param keywords      关键词(标题/主播)
     * @param status        状态筛选，多状态逗号分隔(如 "0,1"；空-全部;0-未开播;1-直播中;2-离开)
     * @param number        返回条数
     * @param isRecommended 是否仅推荐(0-否;1-是；null 或空表示不筛选)
     */
    List<LiveRoomListVO> getDiyRoomList(String keywords, String status, Integer number, Integer isRecommended);

    /**
     * 直播间详情（app：live_info + config）
     */
    LiveRoomDetailVO getRoomDetail(Long id);

    /**
     * 直播间详情（管理端：含推拉流地址与状态名称）
     */
    LiveRoomPageVO getAdminDetail(Long id);

    /**
     * 直播间商品橱窗（goodsdata）
     */
    List<LiveGoodsVO> getRoomGoods(Long roomId);

    // 管理端

    /**
     * 直播间分页列表
     */
    Page<LiveRoomPageVO> getRoomPage(LiveRoomPageQuery query);

    /**
     * 新增直播间
     */
    Long createRoom(LiveRoomForm form);

    /**
     * 编辑直播间
     */
    void updateRoom(LiveRoomForm form);

    /**
     * 删除直播间（含商品挂载）
     */
    void deleteRooms(List<Long> ids);

    /**
     * 绑定直播商品
     */
    void bindGoods(LiveRoomGoodsForm form);

    /**
     * 解绑直播商品
     */
    void unbindGoods(Long goodsId);

    /**
     * 设置讲解商品（同房间其他商品自动取消讲解）
     */
    void setExplainGoods(Long goodsId);

    /**
     * 变更直播状态（开播/离开/下播/封禁）
     */
    void changeStatus(Long id, Integer status);

    /**
     * 重新生成推拉流地址
     */
    LiveRoomPageVO refreshStreamUrl(Long id);

    // 状态/计数（供 WS 与定时任务）

    /**
     * 聊天开关是否开启（WS 层兜底校验；房间不存在视为关闭）
     *
     * @param roomId 直播间ID
     * @return true-聊天开启；false-聊天关闭或房间不存在
     */
    boolean isChatEnabled(Long roomId);

    /**
     * 直播间心跳（刷新 Redis 状态 TTL）
     */
    void heartbeat(Long roomId, Integer status);

    /**
     * 获取房间在线人数（Redis 优先，兜底 DB）
     */
    long getOnlineCount(Long roomId);

    /**
     * 获取房间点赞数（Redis 优先，兜底 DB）
     */
    long getLikeCount(Long roomId);

    /**
     * 累加点赞数
     */
    void addLike(Long roomId, int count);

    /**
     * 构造 WS live-info 推送快照（含讲解商品/在线用户/live_info）
     */
    LiveRoomDetailVO buildLiveInfoPush(Long roomId);

    /**
     * 定时探测：拉流探测 + Redis 心跳双保险，判定下播（供定时任务调用）
     * <p>
     * 已由腾讯云推流/断流回调取代，默认不再执行（见 live.probe-enabled）。
     */
    void autoDetectOfflineRooms();

    // 腾讯云直播回调

    /**
     * 处理腾讯云直播事件回调：签名校验 + 时效校验 + 幂等 + 状态驱动
     * <p>
     * 推流(1) → 自动开播；断流(0) → 自动下播；录制(100)/截图(200) 本期仅记日志。
     *
     * @param dto 回调报文
     * @return true-已受理（含幂等跳过、无关事件、未匹配到房间）；false-签名或时效校验未通过
     */
    boolean handleTencentCallback(LiveCallbackDTO dto);
}
