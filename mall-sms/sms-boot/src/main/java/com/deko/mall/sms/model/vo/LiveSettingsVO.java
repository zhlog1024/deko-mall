package com.deko.mall.sms.model.vo;

import lombok.Data;

/**
 * 全局直播设置视图对象（管理端）
 * <p>注意：不能直接返回 LiveProperties（@RefreshScope Spring 代理，Jackson 序列化报类型错误）
 */
@Data
public class LiveSettingsVO {

    /**
     * 腾讯云直播 SecretId
     */
    private String tencentSecretId;

    /**
     * 腾讯云直播 SecretKey
     */
    private String tencentSecretKey;

    /**
     * 推流域名（rtmp）
     */
    private String pushDomain;

    /**
     * 播放域名（hls）
     */
    private String playDomain;

    /**
     * 推流防盗链 Key
     */
    private String pushAuthKey;

    /**
     * 播放是否走 https
     */
    private Boolean playUseHttps;

    /**
     * WebSocket 主机
     */
    private String wsHost;

    /**
     * WebSocket 端口
     */
    private Integer wsPort;

    /**
     * 是否 wss(1-是;0-否)
     */
    private Integer wsIsWss;

    /**
     * 应用名称
     */
    private String applicationName;
}
