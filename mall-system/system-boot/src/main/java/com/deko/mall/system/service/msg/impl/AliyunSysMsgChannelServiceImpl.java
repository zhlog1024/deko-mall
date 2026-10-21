package com.deko.mall.system.service.msg.impl;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.deko.mall.system.config.AliyunSmsProperties;
import com.deko.mall.system.model.dto.MsgChannelRequest;
import com.deko.mall.system.service.msg.SysMsgChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 阿里云短信渠道实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "sms.type", havingValue = "aliyun")
public class AliyunSysMsgChannelServiceImpl implements SysMsgChannelService {

    private final AliyunSmsProperties aliyunSmsProperties;
    private final ObjectMapper objectMapper;

    @Override
    public boolean sendMsg(MsgChannelRequest request) {
        try {
            String templateParamStr = objectMapper.writeValueAsString(request.getParams());
            String mobile = request.getReceiver();
            String templateCode = request.getTemplate().getThirdTemplateId();

            log.info("阿里云短信发送，手机号：{}，模板：{}，参数：{}", mobile, templateCode, templateParamStr);

            DefaultProfile profile = DefaultProfile.getProfile(aliyunSmsProperties.getRegionId(),
                    aliyunSmsProperties.getAccessKeyId(), aliyunSmsProperties.getAccessKeySecret());
            IAcsClient client = new DefaultAcsClient(profile);

            CommonRequest commonRequest = new CommonRequest();
            commonRequest.setSysMethod(MethodType.POST);
            commonRequest.setSysDomain(aliyunSmsProperties.getDomain());
            commonRequest.setSysVersion("2017-05-25");
            commonRequest.setSysAction("SendSms");
            commonRequest.putQueryParameter("RegionId", aliyunSmsProperties.getRegionId());
            commonRequest.putQueryParameter("PhoneNumbers", mobile);
            commonRequest.putQueryParameter("SignName", aliyunSmsProperties.getSignName());
            commonRequest.putQueryParameter("TemplateCode", templateCode);
            commonRequest.putQueryParameter("TemplateParam", templateParamStr);

            CommonResponse response = client.getCommonResponse(commonRequest);
            boolean success = response.getHttpResponse().isSuccess();

            if (success) {
                log.info("阿里云短信发送成功，手机号：{}", mobile);
            } else {
                log.error("阿里云短信发送失败，手机号：{}，模板：{}，参数：{}", mobile, templateCode, templateParamStr);
            }

            return success;
        } catch (Exception e) {
            log.error("阿里云短信发送异常，手机号：{}，模板：{}", request.getReceiver(), request.getTemplate().getTemplateCode(), e);
            return false;
        }
    }

    @Override
    public String getChannelCode() {
        return "ALIYUN_SMS";
    }

    @Override
    public String getMsgType() {
        return "SMS";
    }
}
