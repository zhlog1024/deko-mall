package com.deko.mall.sms.util;

import cn.hutool.crypto.digest.DigestUtil;
import com.deko.mall.sms.config.LiveProperties;
import com.deko.mall.sms.constant.LiveConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 腾讯云直播推拉流地址生成器
 * <p>
 * 防盗链算法（腾讯云直播官方）：txTime = 过期时间的十六进制(unix秒)；
 * txSecret = MD5(鉴权Key + StreamName + txTime)，取小写。
 * <ul>
 *   <li>推流：rtmp://{推流域名}/live/{StreamName}?txSecret=..&txTime=..&txHost=..</li>
 *   <li>拉流：https://{播放域名}/live/{StreamName}（前端契约固定拼 .m3u8，
 *       故一期播放地址不挂 URL 签名，防盗链用腾讯云控制台域名级 referer/IP 黑白名单）</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class LiveStreamUrlGenerator {

    private final LiveProperties liveProperties;

    /**
     * 生成推流地址（RTMP + 防盗链，有效期默认24小时）
     *
     * @param streamName 流名
     * @return 完整推流地址
     */
    public String generatePushUrl(String streamName) {
        long expire = LocalDateTime.now(ZoneOffset.ofHours(8))
                .plusSeconds(LiveConstants.TENCENT_PUSH_URL_EXPIRE_SECONDS)
                .toEpochSecond(ZoneOffset.ofHours(8));
        String txTime = Long.toHexString(expire);
        String txSecret = DigestUtil.md5Hex(liveProperties.getPushAuthKey() + streamName + txTime);
        return "rtmp://" + liveProperties.getPushDomain() + "/" + LiveConstants.TENCENT_LIVE_APP_NAME + "/"
                + streamName + "?txSecret=" + txSecret + "&txTime=" + txTime
                + "&txHost=" + liveProperties.getPushDomain();
    }

    /**
     * 生成拉流地址前缀（前端契约：前端在此地址后拼 .m3u8）
     *
     * @param streamName 流名
     * @return 拉流地址前缀，如 https://play.example.com/live/abc
     */
    public String generatePullUrl(String streamName) {
        String scheme = Boolean.TRUE.equals(liveProperties.getPlayUseHttps()) ? "https" : "http";
        return scheme + "://" + liveProperties.getPlayDomain() + "/"
                + LiveConstants.TENCENT_LIVE_APP_NAME + "/" + streamName;
    }
}
