package com.deko.mall.sms.model.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

/**
 * 直播间列表项（前端契约：indexdatalist / searchdatalist 的 data_list 元素）
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
public class LiveRoomListVO {

    /**
     * 直播间ID
     */
    private Long id;

    /**
     * 跳转地址（前端 url_event 用，指向直播详情页）
     */
    private String url;

    /**
     * 封面图
     */
    private String cover;

    /**
     * 直播状态(0-未开播;1-直播中;2-离开;3-封禁)
     */
    private Integer status;

    /**
     * 状态名称（前端展示 status_name）
     */
    private String statusName;

    /**
     * 直播标题
     */
    private String title;

    /**
     * 主播昵称
     */
    private String sellerName;

    /**
     * 在线人数
     */
    private Integer onlineCount;
}
