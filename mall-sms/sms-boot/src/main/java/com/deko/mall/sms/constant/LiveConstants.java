package com.deko.mall.sms.constant;

/**
 * 直播模块常量（Redis Key / WS 协议 / 前端契约 URL）
 * <p>
 * Redis Key 规范：`sms:live:{域}:{id}`，TTL 见常量注释
 */
public final class LiveConstants {

    private LiveConstants() {
    }

    // Redis Key

    /** 直播间状态心跳 Key —— TTL 为心跳窗口，超时视为异常（由定时任务探测兜底） */
    public static final String LIVE_ROOM_STATUS_PREFIX = "sms:live:room:status:";
    public static final long LIVE_ROOM_STATUS_TTL_SECONDS = 20L;

    /** 直播间拉流探测失败计数 Key */
    public static final String LIVE_ROOM_PROBE_FAIL_PREFIX = "sms:live:room:probe-fail:";

    /** 拉流探测连续失败阈值（达到即自动下播，防瞬时抖动误判） */
    public static final long LIVE_ROOM_PROBE_FAIL_THRESHOLD = 2L;

    /** 直播间在线人数计数 Key（WS 连接数） */
    public static final String LIVE_ROOM_ONLINE_PREFIX = "sms:live:room:online:";

    /** 直播间点赞数计数 Key */
    public static final String LIVE_ROOM_LIKE_PREFIX = "sms:live:room:like:";

    /** 计数类 Key 兜底 TTL（防脏数据堆积） */
    public static final long LIVE_ROOM_COUNT_TTL_SECONDS = 24 * 60 * 60L;

    // WS 协议消息类型（前端契约）

    /** 初始化（客户端→服务端 / 服务端→客户端） */
    public static final String WS_TYPE_INIT = "init";
    /** 初始化成功（服务端→客户端） */
    public static final String WS_TYPE_INIT_SUCCESS = "init-success";
    /** 初始化失败（服务端→客户端） */
    public static final String WS_TYPE_INIT_FAIL = "init-fail";
    /** 加入直播间提示（服务端→客户端广播） */
    public static final String WS_TYPE_JOIN = "join";
    /** 聊天消息（双向） */
    public static final String WS_TYPE_MESSAGE = "message";
    /** 直播间实时数据（双向，服务端按请求/定时推送） */
    public static final String WS_TYPE_LIVE_INFO = "live-info";
    /** 点赞（客户端→服务端，content 为本次点赞数） */
    public static final String WS_TYPE_LIVE_LIKE = "live-like";
    /** 心跳 ping（客户端→服务端） */
    public static final String WS_TYPE_PING = "ping";

    /** 服务端下发的 ping 间隔（秒） */
    public static final int WS_PING_INTERVAL = 30;

    /** live-info 定时推送间隔（秒） */
    public static final int WS_LIVE_INFO_PUSH_INTERVAL_SECONDS = 5;

    /**
     * 前端主动拉取 live-info 的间隔（秒），作为服务端 5 秒推送之外的兜底轮询。
     * <p>
     * 必须下发：前端 live-content.vue 会用它做 `setInterval(..., n * 1000)`，
     * 若该字段缺失会被赋为 undefined → `NaN * 1000` → setInterval 间隔按 0 处理，
     * 导致疯狂空转刷屏（前端本地默认值会被 undefined 覆盖）。
     */
    public static final int LIVE_INFO_POLL_INTERVAL_SECONDS = 30;

    // 前端契约 URL

    /** 直播详情页前缀（前端 url 字段） */
    public static final String LIVE_DETAIL_PAGE_PREFIX = "/pages/plugins/live/detail/detail?id=";

    /** 商品详情页前缀（直播商品橱窗 goods_url） */
    public static final String GOODS_DETAIL_PAGE_PREFIX = "/pages/goods-detail/goods-detail?id=";

    // 腾讯云直播

    /** 推流应用名（腾讯云直播默认 live） */
    public static final String TENCENT_LIVE_APP_NAME = "live";

    /** 防盗链有效期（秒，推流地址） */
    public static final long TENCENT_PUSH_URL_EXPIRE_SECONDS = 24 * 60 * 60L;

    /** 事件回调接收路径（对外为 https://mall-api.example.com/mall-sms + 本路径） */
    public static final String TENCENT_CALLBACK_PATH = "/callback-api/v1/live/tencent";

    /**
     * 回调签名允许的时钟偏差（秒）
     * <p>
     * 回调里的 t 是「过期时间」，正常应满足 now &lt;= t；
     * 放宽该值以容忍腾讯云与本机的时钟偏差。
     */
    public static final long TENCENT_CALLBACK_CLOCK_SKEW_SECONDS = 60L;

    // 直播列表排序（前端 search_order_by_list / by 参数）

    /** 默认排序：按 sort 升序 */
    public static final String LIVE_ORDER_BY_DEFAULT = "default";

    /** 人气排序：按点赞数降序 */
    public static final String LIVE_ORDER_BY_HOT = "hot";

    /** 最新排序：按创建时间降序 */
    public static final String LIVE_ORDER_BY_NEW = "new";
}
