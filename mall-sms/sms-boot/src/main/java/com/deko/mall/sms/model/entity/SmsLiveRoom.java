package com.deko.mall.sms.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 直播间表
 */
@TableName(value = "sms_live_room")
@Data
public class SmsLiveRoom extends BaseEntity {
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 直播间编号
     */
    private String roomNo;

    /**
     * 直播标题
     */
    private String title;

    /**
     * 直播间封面图
     */
    private String cover;

    /**
     * 直播流名(唯一,用于生成推拉流地址)
     */
    private String streamName;

    /**
     * 推流地址(RTMP,含防盗链,可重新生成)
     */
    private String pushUrl;

    /**
     * 拉流地址(HLS前缀,前端拼.m3u8)
     */
    private String pullUrl;

    /**
     * 直播状态(0-未开播;1-直播中;2-主播离开;3-封禁)
     */
    private Integer status;

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
     * 点赞数
     */
    private Integer likeCount;

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
     * 直播间欢迎语
     */
    private String liveTips;

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
     * 开播时间
     */
    private LocalDateTime liveStartTime;

    /**
     * 下播时间
     */
    private LocalDateTime liveEndTime;

    /**
     * 是否推荐(0-否;1-是)。供装修页「直播列表」组件的推荐筛选使用
     */
    private Integer isRecommended;

    /**
     * 排序(越小越靠前)
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;

    /**
     * 逻辑删除标识(0-正常;1-删除)
     */
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
}
