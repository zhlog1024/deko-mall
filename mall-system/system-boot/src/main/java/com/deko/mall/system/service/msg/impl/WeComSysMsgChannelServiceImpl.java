package com.deko.mall.system.service.msg.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.deko.mall.system.config.WeComProperties;
import com.deko.mall.system.model.dto.MsgChannelRequest;
import com.deko.mall.system.service.msg.SysMsgChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 企业微信群机器人消息渠道实现
 * <p>
 * 通过群机器人 Webhook 发送 Markdown 消息到企业微信群。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeComSysMsgChannelServiceImpl implements SysMsgChannelService {

    private final WeComProperties weComProperties;

    @Override
    public boolean sendMsg(MsgChannelRequest request) {
        String webhookUrl = weComProperties.getWebhookUrl();
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("企微 Webhook URL 为空，跳过推送");
            return false;
        }

        String content = request.getContent();
        if (content == null || content.isBlank()) {
            log.warn("企微推送内容为空，跳过推送");
            return false;
        }

        try {
            Map<String, Object> body = Map.of(
                    "msgtype", "markdown",
                    "markdown", Map.of("content", content)
            );
            String resp = HttpUtil.post(webhookUrl, JSONUtil.toJsonStr(body));
            log.info("企微消息推送完成, webhook: {}, resp: {}", maskWebhook(webhookUrl), resp);
            return true;
        } catch (Exception e) {
            log.error("企微消息推送失败, webhook: {}", maskWebhook(webhookUrl), e);
            return false;
        }
    }

    @Override
    public String getChannelCode() {
        return "WECOM_BOT";
    }

    @Override
    public String getMsgType() {
        return "BOT";
    }

    /**
     * 脱敏 Webhook URL（仅保留 key 前几位）
     */
    private String maskWebhook(String url) {
        if (url == null || url.length() < 30) return url;
        int keyIdx = url.indexOf("key=");
        if (keyIdx < 0) return url;
        String prefix = url.substring(0, keyIdx + 4);
        String suffix = url.substring(Math.min(keyIdx + 12, url.length()));
        return prefix + "***" + suffix;
    }
}
