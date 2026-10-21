package com.deko.mall.auth.service;

import cn.hutool.captcha.AbstractCaptcha;
import cn.hutool.core.util.IdUtil;
import com.deko.mall.auth.config.CaptchaProperties;
import com.deko.mall.auth.model.CaptchaResult;
import com.deko.mall.common.constant.RedisConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 认证服务
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final CaptchaProperties captchaProperties;
    private final CaptchaService captchaService;

    private final StringRedisTemplate redisTemplate;


    /**
     * 获取图形验证码
     *
     * @return Result<CaptchaResult>
     */
    public CaptchaResult getCaptcha() {

        AbstractCaptcha captcha = captchaService.generate();

        // 验证码文本缓存至Redis，用于登录校验
        String captchaId = IdUtil.fastSimpleUUID();
        redisTemplate.opsForValue().set(
                RedisConstants.CAPTCHA_CODE_PREFIX + captchaId,
                captcha.getCode(),
                captchaProperties.getExpireSeconds(),
                TimeUnit.SECONDS
        );

        CaptchaResult captchaResult = CaptchaResult.builder()
                .captchaId(captchaId)
                .captchaBase64(captcha.getImageBase64Data())
                .build();

        return captchaResult;
    }


}
