package com.deko.mall.sms.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.deko.mall.sms.constant.LiveConstants;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

/**
 * 腾讯云直播回调签名工具
 * <p>
 * 官方算法：<b>sign = MD5(key + t)</b>，其中 key 为控制台「功能配置 &gt; 直播回调」中的回调密钥。
 * t 为过期时间（UNIX 秒），官方默认 10 分钟有效；过期即判无效，用于防止网络重放攻击。
 * <p>
 * 文档：https://www.tencentcloud.com/zh/document/product/267/38081
 */
public final class LiveCallbackSignUtils {

    private LiveCallbackSignUtils() {
    }

    /**
     * 按官方算法计算签名
     *
     * @param key 回调密钥
     * @param t   过期时间（UNIX 秒）
     * @return 小写十六进制 MD5
     */
    public static String sign(String key, Long t) {
        return DigestUtil.md5Hex(key + t);
    }

    /**
     * 校验签名
     * <p>
     * 用 {@link MessageDigest#isEqual} 做常量时间比较，避免逐字符比较带来的时序侧信道。
     *
     * @param key  回调密钥
     * @param t    过期时间（UNIX 秒）
     * @param sign 回调携带的签名
     * @return true-签名正确
     */
    public static boolean verify(String key, Long t, String sign) {
        if (StrUtil.isBlank(key) || t == null || StrUtil.isBlank(sign)) {
            return false;
        }
        byte[] expected = sign(key, t).getBytes(StandardCharsets.UTF_8);
        byte[] actual = sign.trim().toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, actual);
    }

    /**
     * 时间戳是否已过期（防重放）
     * <p>
     * t 是「过期时间」而非「产生时间」，故正常情况应满足 now &lt;= t；
     * 额外放宽 {@link LiveConstants#TENCENT_CALLBACK_CLOCK_SKEW_SECONDS} 秒，容忍两台机器的时钟偏差。
     *
     * @param t 过期时间（UNIX 秒）
     * @return true-已过期
     */
    public static boolean isExpired(Long t) {
        if (t == null) {
            return true;
        }
        long now = System.currentTimeMillis() / 1000L;
        return now > t + LiveConstants.TENCENT_CALLBACK_CLOCK_SKEW_SECONDS;
    }
}
