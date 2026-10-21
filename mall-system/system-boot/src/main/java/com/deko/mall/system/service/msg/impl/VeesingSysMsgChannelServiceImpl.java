package com.deko.mall.system.service.msg.impl;

import com.deko.mall.system.config.VeesingSmsProperties;
import com.deko.mall.system.model.dto.MsgChannelRequest;
import com.deko.mall.system.service.msg.SysMsgChannelService;
import com.deko.mall.system.service.msg.SysMsgTemplateService;
import com.deko.mall.system.utils.OkHttpUtil;
import com.deko.mall.system.utils.XmlUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 维信短信渠道实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "sms.type", havingValue = "veesing")
public class VeesingSysMsgChannelServiceImpl implements SysMsgChannelService {

    private static final OkHttpUtil HTTP_UTIL = new OkHttpUtil();
    private final VeesingSmsProperties veesingSmsProperties;

    private final SysMsgTemplateService templateService;

    @Override
    public boolean sendMsg(MsgChannelRequest request) {
        try {
            // 维信不支持模板，需要直接发送内容
            String content = templateService.renderTemplate(request.getTemplate(), request.getParams());
            // 维信平台要求11位纯数字手机号，去除+86/0086等国际区号前缀
            String mobile = stripCountryCode(request.getReceiver());

            Map<String, String> param = new HashMap<>(4);
            param.put("account", veesingSmsProperties.getAccount());
            param.put("password", veesingSmsProperties.getPassword());
            param.put("content", content);
            param.put("mobile", mobile);

            log.info("维信平台发送短信，手机号：{}，内容：{}", mobile, content);
            String result = HTTP_UTIL.postForm(veesingSmsProperties.getUrl(), param);

            Map<String, String> response = XmlUtils.parseMap(result);
            String resCode = response.get("code");
            String smsId = response.get("smsid");
            String msg = response.get("msg");

            log.info("维信平台响应结果：code={}，smsid={}，msg={}", resCode, smsId, msg);

            if ("2".equals(resCode)) {
                return true;
            }

            log.error("维信短信发送失败，手机号：{}，错误信息：{}", mobile, msg);
            return false;
        } catch (Exception e) {
            log.error("维信短信发送异常，手机号：{}，模板：{}", request.getReceiver(), request.getTemplate().getTemplateCode(), e);
            return false;
        }
    }

    @Override
    public String getChannelCode() {
        return "VEESING_SMS";
    }

    @Override
    public String getMsgType() {
        return "SMS";
    }

    /**
     * 去除手机号的国际区号前缀（如 +86、0086），返回11位纯数字
     */
    private String stripCountryCode(String mobile) {
        if (mobile == null || mobile.isEmpty()) {
            return mobile;
        }
        // 去除 +86 前缀
        if (mobile.startsWith("+86")) {
            return mobile.substring(3);
        }
        // 去除 0086 前缀
        if (mobile.startsWith("0086")) {
            return mobile.substring(4);
        }
        return mobile;
    }
}
