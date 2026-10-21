package com.deko.mall.sms.model.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

/**
 * WebSocket 连接配置（前端契约：config.socket_connect{host,port,is_wss,path}）
 * <p>
 * 前端拼成 {@code ws(s)://{host}:{port}{path}}。
 * <p>
 * path 用于「经统一网关」的场景：生产为 {@code /mall-sms/}，网关 StripPrefix 后
 * 后端收到的仍是 {@code /}（与 WebSocketConfig 注册的根路径一致）；
 * 本地直连服务时 path 为 {@code /}。
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
public class LiveSocketConnectVO {

    /**
     * 主机
     */
    private String host;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 是否 wss(1-是;0-否)
     */
    private Integer isWss;

    /**
     * 连接路径（以 / 开头；经网关时为网关前缀，如 /mall-sms/）
     */
    private String path;
}
