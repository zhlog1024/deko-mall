package com.deko.mall.sms.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 直播间新增/编辑表单（管理端）
 */
@Data
public class LiveRoomForm {

    @Schema(description = "直播间ID(编辑时必传)")
    private Long id;

    @Schema(description = "直播标题")
    private String title;

    @Schema(description = "直播间封面图")
    private String cover;

    @Schema(description = "直播流名(唯一;不传则自动生成)")
    private String streamName;

    @Schema(description = "主播昵称")
    private String anchorName;

    @Schema(description = "主播头像")
    private String anchorAvatar;

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

    @Schema(description = "是否推荐(0-否;1-是)，装修页直播组件「推荐」筛选用")
    private Integer isRecommended;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;
}
