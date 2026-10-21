package com.deko.mall.system.model.dto;

import com.deko.mall.system.model.entity.SysMsgTemplate;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * 消息发送请求
 */
@Data
@Builder
public class MsgChannelRequest {
    /**
     * 接收者（短信为手机号，企微机器人可为空）
     */
    private String receiver;

    /**
     * 消息模板
     */
    private SysMsgTemplate template;

    /**
     * 模板参数
     */
    private Map<String, Object> params;

    /**
     * 预渲染的消息内容（企微机器人等渠道使用）
     * 如果不为空，渠道可直接使用此内容发送
     */
    private String content;
}
