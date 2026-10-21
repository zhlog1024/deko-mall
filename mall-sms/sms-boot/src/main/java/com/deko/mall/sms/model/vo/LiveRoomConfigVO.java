package com.deko.mall.sms.model.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

/**
 * 直播间功能配置（前端契约：detail 的 config）
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
public class LiveRoomConfigVO {

    /**
     * 是否开启聊天(0-否;1-是)
     */
    private Integer isLiveChat;

    /**
     * 是否开启商品购买(0-否;1-是)
     */
    private Integer isLiveGoodsBuy;

    /**
     * 是否开启点赞(0-否;1-是)
     */
    private Integer isLiveLike;

    /**
     * 视频断流重连次数
     */
    private Integer liveReconnectNumber;

    /**
     * 视频重连间隔(秒)
     */
    private Integer liveReconnectIntervalTime;

    /**
     * 讲解商品自动关闭时间(秒)
     */
    private Integer liveGoodsExplainAutoCloseTime;

    /**
     * 前端主动拉取 live-info 的间隔(秒)
     * <p>
     * 前端以此作为 setInterval 周期，缺失会导致间隔变成 NaN(≈0) 疯狂空转，务必下发。
     */
    private Integer liveInfoTimingIntervalTime;

    /**
     * 直播间欢迎语
     */
    private String liveTips;

    /**
     * WebSocket 连接配置
     */
    private LiveSocketConnectVO socketConnect;
}
