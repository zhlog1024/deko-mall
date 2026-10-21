package com.deko.mall.sms.model.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;

/**
 * 直播间详情数据（前端契约：detail 返回的 data）
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
public class LiveRoomDetailVO {

    /**
     * 直播间ID
     * <p>
     * 【必须下发】前端把本对象整体赋给 live_data，而 live_data.id 被用于：
     * ① WS init 消息的 live_id —— 缺失会被服务端判为「直播间ID缺失」导致无法入房（收不到任何广播）；
     * ② 商品橱窗的 propLiveId —— 缺失会请求 /rooms/undefined/goods 而加载失败；
     * ③ 分享链接的 id 参数。
     */
    private Long id;

    /**
     * 直播间实时信息
     */
    private LiveRoomInfoVO liveInfo;

    /**
     * 直播间功能配置
     */
    private LiveRoomConfigVO config;

    /**
     * 直播标题（live_data.title 兜底展示）
     */
    private String title;

    /**
     * 封面图（live_data.cover 兜底展示）
     */
    private String cover;

    /**
     * 主播昵称
     */
    private String anchorName;

    /**
     * 主播头像
     */
    private String anchorAvatar;

    /**
     * 在线人数
     */
    private Integer onlineCount;

    /**
     * 当前讲解商品（WS live-info explain_goods；无讲解时为 null）
     */
    private LiveGoodsVO explainGoods;

    /**
     * 在线观众列表（前端模板按 item.avatar 取值，必须是对象数组而非字符串数组）
     */
    private List<LiveOnlineUserVO> onlineUser;
}
