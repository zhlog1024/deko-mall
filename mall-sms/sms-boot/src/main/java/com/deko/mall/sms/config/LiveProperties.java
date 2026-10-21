package com.deko.mall.sms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * 直播功能配置（nacos: mall-sms.yaml → live.*）
 * <p>
 * 含腾讯云直播（推拉流域名/防盗链Key）与 WebSocket 聊天服务地址（前端契约 socket_connect 下发）
 */
@RefreshScope
@Component
@ConfigurationProperties(prefix = "live")
@Data
public class LiveProperties {

    /**
     * 腾讯云直播 SecretId（暂用于扩展：录制/审核等）
     */
    private String tencentSecretId = "";

    /**
     * 腾讯云直播 SecretKey（暂用于扩展）
     */
    private String tencentSecretKey = "";

    /**
     * 推流域名（rtmp，如 push.example.com）
     */
    private String pushDomain = "";

    /**
     * 播放域名（https，如 play.example.com）
     */
    private String playDomain = "";

    /**
     * 推流防盗链 Key（腾讯云直播控制台 - 推流鉴权配置）
     */
    private String pushAuthKey = "";

    /**
     * 播放是否走 https
     */
    private Boolean playUseHttps = true;

    /**
     * WebSocket 服务地址（下发到前端 config.socket_connect）
     */
    private String wsHost = "127.0.0.1";

    /**
     * WebSocket 服务端口
     */
    private Integer wsPort = 8804;

    /**
     * 是否 wss（1-是；0-否，前端契约 is_wss）
     */
    private Integer wsIsWss = 0;

    /**
     * WebSocket 连接路径（前端拼成 ws(s)://{host}:{port}{path}）
     * <p>
     * 默认 {@code /} —— 本地直连 mall-sms 的 8804，无网关前缀。
     * <p>
     * 生产走统一网关（mall-api.example.com），需填 {@code /mall-sms/}：
     * 网关按 Path=/mall-sms/** 匹配并 StripPrefix 掉该前缀，
     * mall-sms 收到的仍是 {@code /}，与 {@code WebSocketConfig} 里注册的根路径一致，
     * 因此服务端 handler 无需改动。
     */
    private String wsPath = "/";

    /**
     * 应用名称（前端 base.application_name / seo_title 兜底展示）
     */
    private String applicationName = "健康公社";

    /**
     * 腾讯云直播回调密钥（控制台「功能配置 &gt; 直播回调」中配置），用于校验回调签名 sign = MD5(key + t)。
     * <p>
     * 留空则<b>跳过签名校验</b>并打 WARN 日志 —— 仅便于本地联调，生产必须配置。
     */
    private String callbackKey = "";

    /**
     * 推流/断流回调是否驱动直播间状态（自动开播/下播）。
     * <p>
     * 关闭后回调只记日志不改状态，用于灰度或排障。
     */
    private Boolean callbackDriveStatus = true;

    /**
     * 拉流探测开关。
     * <p>
     * 状态已改由腾讯云推流/断流回调驱动，且探测目标（拉流域名）在当前环境恒不可达，
     * 会导致「直播中」约 20 秒后被误判下播，故默认关闭。
     * 保留开关是为了出问题时可一键回退，且回退无需发版。
     */
    private Boolean probeEnabled = false;
}
