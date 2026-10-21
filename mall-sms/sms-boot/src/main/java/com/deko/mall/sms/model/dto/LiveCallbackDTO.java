package com.deko.mall.sms.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

/**
 * 腾讯云直播事件回调报文
 * <p>
 * 字段对齐官方文档《推断流事件通知》《录制文件事件通知》：
 * https://www.tencentcloud.com/zh/document/product/267/38081
 * <p>
 * 腾讯云下发的是 snake_case；不同 event_type 携带的字段集合不同
 * （例如 push_duration 仅断流有、file_id/video_url 仅录制有），
 * 故开启 ignoreUnknown，未声明字段一律忽略，避免新增事件字段导致反序列化失败。
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LiveCallbackDTO {

    /**
     * 过期时间（UNIX 秒）。官方默认 10 分钟内有效，用于防重放
     */
    private Long t;

    /**
     * 安全签名 = MD5(回调密钥 + t)
     */
    private String sign;

    /**
     * 事件类型：0-断流；1-推流；100-新录制文件；200-新截图文件
     */
    private Integer eventType;

    /**
     * 用户 APPID
     */
    private Integer appid;

    /**
     * 推流域名
     */
    private String app;

    /**
     * 推流路径
     */
    private String appname;

    /**
     * 直播流名，对应 SmsLiveRoom.streamName
     */
    private String streamId;

    /**
     * 直播流名（与 streamId 同值，历史原因保留两个名字）
     */
    private String channelId;

    /**
     * 事件产生时间（UNIX 秒）
     */
    private Long eventTime;

    /**
     * 消息序列号：同一次推流活动产生的推流与断流消息序列号相同
     */
    private String sequence;

    /**
     * 推流接入点 IP
     */
    private String node;

    /**
     * 用户推流 IP
     */
    private String userIp;

    /**
     * 用户推流 URL 所带参数
     */
    private String streamParam;

    /**
     * 推流时长（毫秒，仅断流事件携带）
     */
    private String pushDuration;

    /**
     * 推断流错误码
     */
    private Integer errcode;

    /**
     * 推断流错误描述
     */
    private String errmsg;
}
