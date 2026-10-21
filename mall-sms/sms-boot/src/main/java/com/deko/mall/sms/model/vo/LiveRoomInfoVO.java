package com.deko.mall.sms.model.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

/**
 * 直播间实时信息（前端契约：detail 的 live_info；WS live-info 的 live_info）
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
public class LiveRoomInfoVO {

    /**
     * 直播间ID
     */
    private Long id;

    /**
     * 直播标题
     */
    private String title;

    /**
     * 封面图
     */
    private String cover;

    /**
     * 拉流地址前缀（前端拼 .m3u8 播放）
     */
    private String pullUrl;

    /**
     * 直播状态(0-未开播;1-直播中;2-离开;3-封禁)
     */
    private Integer status;

    /**
     * 在线人数
     */
    private Integer onlineCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 主播昵称
     */
    private String anchorName;

    /**
     * 主播头像
     */
    private String anchorAvatar;

    /**
     * 开播时间
     */
    private String liveStartTime;

    /**
     * 下播时间
     */
    private String liveEndTime;
}
