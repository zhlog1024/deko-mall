package com.deko.mall.common.utils;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 企业微信机器人消息推送工具
 *
 * @deprecated 请使用 mall-system 的 SysMsgService + WECOM_BOT 消息渠道替代。
 *             通过 RabbitMQ 发送 SystemNotifyMessage 到 system.notify.exchange，
 *             mall-system 消费后自动通过模板渲染并推送到企微群机器人。
 */
@Deprecated
@Slf4j
public class WeComNotifyUtils {

    private WeComNotifyUtils() {
    }

    /**
     * 发送 Markdown 消息到企业微信群机器人
     *
     * @param webhookUrl 机器人 webhook 地址
     * @param content    Markdown 内容（企微 Markdown 语法）
     */
    public static void sendMarkdown(String webhookUrl, String content) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("企微 webhook URL 为空，跳过推送");
            return;
        }
        try {
            Map<String, Object> body = Map.of(
                    "msgtype", "markdown",
                    "markdown", Map.of("content", content)
            );
            String resp = HttpUtil.post(webhookUrl, JSONUtil.toJsonStr(body));
            log.info("企微消息推送完成, resp: {}", resp);
        } catch (Exception e) {
            log.error("企微消息推送失败", e);
        }
    }
}
