package com.deko.mall.system.service.msg;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.common.constant.RedisConstants;
import com.deko.mall.common.web.exception.BizException;
import com.deko.mall.system.config.SysResultCode;
import com.deko.mall.system.mapper.MsgRecordMapper;
import com.deko.mall.system.model.dto.MsgChannelRequest;
import com.deko.mall.system.model.entity.SysMsgRecord;
import com.deko.mall.system.model.entity.SysMsgTemplate;
import com.deko.mall.system.model.query.SysMsgRecordQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 消息服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysMsgService extends ServiceImpl<MsgRecordMapper, SysMsgRecord> {

    private final SysMsgChannelFactory sysMsgChannelFactory;
    private final StringRedisTemplate redisTemplate;
    private final MsgRecordMapper msgRecordMapper;
    private final SysMsgTemplateService templateService;

    /**
     * 发送登录短信验证码（兼容旧接口）
     */
    public boolean sendLoginSmsCode(String mobile) {
        return sendLoginSmsCode(mobile, "+86");
    }

    /**
     * 发送登录短信验证码（支持国家区号）
     */
    public boolean sendLoginSmsCode(String mobile, String countryCode) {
        return sendSmsCode(mobile, countryCode, "LOGIN_SMS");
    }

    /**
     * 发送注册短信验证码（兼容旧接口）
     */
    public boolean sendRegisterSmsCode(String mobile) {
        return sendRegisterSmsCode(mobile, "+86");
    }

    /**
     * 发送注册短信验证码（支持国家区号）
     */
    public boolean sendRegisterSmsCode(String mobile, String countryCode) {
        return sendSmsCode(mobile, countryCode, "REGISTER_SMS");
    }

    /**
     * 发送短信验证码（兼容旧接口）
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean sendSmsCode(String mobile, String templateCode) {
        return sendSmsCode(mobile, "+86", templateCode);
    }

    /**
     * 发送短信验证码（支持国家区号）
     *
     * @param mobile       本地手机号
     * @param countryCode  国家区号(如+86)，默认+86
     * @param templateCode 模板编码
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean sendSmsCode(String mobile, String countryCode, String templateCode) {
        if (countryCode == null || countryCode.isBlank()) {
            countryCode = "+86";
        }
        // 拼接完整国际号码（去掉+号后拼接，因为阿里云等SMS SDK可能需要纯数字）
        String fullMobile = countryCode + mobile;
        // 1. 获取消息模板
        SysMsgTemplate template = templateService.getTemplate(templateCode);
        // 2. 获取消息渠道服务
        SysMsgChannelService channelService = sysMsgChannelFactory.getMsgChannelService(template.getChannelCode());
        if (channelService == null) {
            throw new BizException(SysResultCode.MSG_CHANNEL_NOT_FOUND);
        }
        // 3. 生成验证码
        String code = RandomUtil.randomNumbers(6);
        // 4. 构建请求参数
        Map<String, Object> templateParam = new HashMap<>(1);
        templateParam.put("code", code);
        // 5. 渲染模板内容
        String content = templateService.renderTemplate(template, templateParam);
        // 6. 创建消息记录
        SysMsgRecord msgRecord = SysMsgRecord.builder()
                .receiver(fullMobile)
                .templateCode(templateCode)
                .templateParams(JSONUtil.toJsonStr(templateParam))
                .content(content)
                .channelCode(template.getChannelCode())
                .msgType(channelService.getMsgType())
                .status(0) // 初始状态：待发送
                .build();
        msgRecordMapper.insert(msgRecord);
        // 7. 构建消息请求（传完整国际号码给SMS渠道）
        MsgChannelRequest request = MsgChannelRequest.builder()
                .receiver(fullMobile)
                .template(template)
                .params(templateParam)
                .build();
        // 8. 发送消息
        boolean result = channelService.sendMsg(request);
        // 9. 更新发送状态
        msgRecord.setStatus(result ? 1 : 2); // 1: 发送成功, 2: 发送失败
        msgRecordMapper.updateById(msgRecord);
        // 10. 保存验证码（Redis key 包含国家区号，避免不同国家同号段冲突）
        if (result) {
            redisTemplate.opsForValue()
                    .set(RedisConstants.LOGIN_SMS_CODE_PREFIX + countryCode + ":" + mobile, code, 5, TimeUnit.MINUTES);
        }

        return result;
    }

    /**
     * 通过模板发送消息（通用方法，支持企微/短信等所有渠道）
     *
     * @param templateCode 模板编码
     * @param params       模板参数
     * @param businessId   业务ID（用于追溯）
     * @param source       消息来源（如 PMS_SERVICE, UMS_SERVICE）
     * @return 是否发送成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean sendByTemplate(String templateCode, Map<String, Object> params,
                                   String businessId, String source) {
        // 1. 获取消息模板
        SysMsgTemplate template = templateService.getTemplate(templateCode);

        // 2. 获取消息渠道服务
        SysMsgChannelService channelService = sysMsgChannelFactory.getMsgChannelService(template.getChannelCode());
        if (channelService == null) {
            throw new BizException(SysResultCode.MSG_CHANNEL_NOT_FOUND);
        }

        // 3. 渲染模板内容
        String content = templateService.renderTemplate(template, params);
        String templateParamsJson = JSONUtil.toJsonStr(params);

        // 4. 创建消息记录
        SysMsgRecord msgRecord = SysMsgRecord.builder()
                .receiver("") // BOT 类型消息无特定接收人，SMS 场景由调用方设置
                .templateCode(templateCode)
                .templateParams(templateParamsJson)
                .content(content)
                .channelCode(template.getChannelCode())
                .msgType(channelService.getMsgType())
                .status(0) // 初始状态：待发送
                .build();
        msgRecordMapper.insert(msgRecord);

        // 5. 构建消息请求（含预渲染内容）
        MsgChannelRequest request = MsgChannelRequest.builder()
                .template(template)
                .params(params)
                .content(content)
                .build();

        // 6. 发送消息
        boolean result;
        try {
            result = channelService.sendMsg(request);
        } catch (Exception e) {
            log.error("消息发送异常: templateCode={}, businessId={}, source={}, error={}",
                    templateCode, businessId, source, e.getMessage(), e);
            result = false;
        }

        // 7. 更新发送状态
        msgRecord.setStatus(result ? 1 : 2); // 1: 发送成功, 2: 发送失败
        if (!result) {
            msgRecord.setErrorMsg("消息发送失败");
        }
        msgRecordMapper.updateById(msgRecord);

        log.info("模板消息发送完成: templateCode={}, channel={}, result={}, businessId={}",
                templateCode, template.getChannelCode(), result, businessId);

        return result;
    }

    /**
     * 分页查询短信记录
     */
    public IPage<SysMsgRecord> listRecordsPage(SysMsgRecordQuery msgRecordQuery) {
        // 参数构建
        int pageNum = msgRecordQuery.getPageNum();
        int pageSize = msgRecordQuery.getPageSize();
        Page<SysMsgRecord> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<SysMsgRecord> queryWrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(msgRecordQuery.getReceiver())) {
            queryWrapper.like(SysMsgRecord::getReceiver, msgRecordQuery.getReceiver());
        }
        if (msgRecordQuery.getStatus() != null) {
            queryWrapper.eq(SysMsgRecord::getStatus, msgRecordQuery.getStatus());
        }
        // 按创建时间倒序
        queryWrapper.orderByDesc(SysMsgRecord::getCreateTime);

        return msgRecordMapper.selectPage(page, queryWrapper);
    }
}
