package com.deko.mall.system.service.msg;

import com.deko.mall.system.model.dto.MsgChannelRequest;

/**
 * 消息渠道服务接口
 */
public interface SysMsgChannelService {

    /**
     * 发送消息
     *
     * @param request 消息请求
     * @return 发送结果
     */
    boolean sendMsg(MsgChannelRequest request);

    /**
     * 获取渠道编码
     *
     * @return 渠道编码
     */
    String getChannelCode();

    /**
     * 获取消息类型
     *
     * @return 消息类型
     */
    String getMsgType();
}
