package com.deko.mall.sms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 直播间分页视图对象（管理端）
 * <p>含功能开关字段，供编辑回填
 */
@Data
public class LiveRoomPageVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "直播间编号")
    private String roomNo;

    @Schema(description = "直播标题")
    private String title;

    @Schema(description = "封面图")
    private String cover;

    @Schema(description = "直播流名")
    private String streamName;

    @Schema(description = "推流地址")
    private String pushUrl;

    @Schema(description = "拉流地址")
    private String pullUrl;

    @Schema(description = "直播状态(0-未开播;1-直播中;2-离开;3-封禁)")
    private Integer status;

    @Schema(description = "直播状态名称")
    private String statusName;

    @Schema(description = "主播昵称")
    private String anchorName;

    @Schema(description = "主播头像")
    private String anchorAvatar;

    @Schema(description = "在线人数")
    private Integer onlineCount;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "开播时间")
    private LocalDateTime liveStartTime;

    @Schema(description = "下播时间")
    private LocalDateTime liveEndTime;

    @Schema(description = "是否推荐(0-否;1-是)")
    private Integer isRecommended;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "是否开启聊天(0-否;1-是)")
    private Integer isLiveChat;

    @Schema(description = "是否开启商品购买(0-否;1-是)")
    private Integer isLiveGoodsBuy;

    @Schema(description = "是否开启点赞(0-否;1-是)")
    private Integer isLiveLike;

    @Schema(description = "直播间欢迎语")
    private String liveTips;

    @Schema(description = "视频断流重连次数")
    private Integer liveReconnectNumber;

    @Schema(description = "视频重连间隔(秒)")
    private Integer liveReconnectIntervalTime;

    @Schema(description = "讲解商品自动关闭时间(秒)")
    private Integer liveGoodsExplainAutoCloseTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
